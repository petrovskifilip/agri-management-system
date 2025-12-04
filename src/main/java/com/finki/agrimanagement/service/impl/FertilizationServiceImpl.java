package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.FertilizationRequestDTO;
import com.finki.agrimanagement.dto.response.FertilizationResponseDTO;
import com.finki.agrimanagement.entity.Farm;
import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.FertilizationStatus;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.FertilizationMapper;
import com.finki.agrimanagement.repository.FarmRepository;
import com.finki.agrimanagement.repository.FertilizationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.EmailNotificationService;
import com.finki.agrimanagement.service.FertilizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class FertilizationServiceImpl implements FertilizationService {

    private final FertilizationRepository fertilizationRepository;
    private final ParcelRepository parcelRepository;
    private final FarmRepository farmRepository;
    private final FertilizationMapper fertilizationMapper;
    private final EmailNotificationService emailNotificationService;

    public FertilizationServiceImpl(FertilizationRepository fertilizationRepository,
                                    ParcelRepository parcelRepository,
                                    FarmRepository farmRepository,
                                    FertilizationMapper fertilizationMapper,
                                    EmailNotificationService emailNotificationService) {
        this.fertilizationRepository = fertilizationRepository;
        this.parcelRepository = parcelRepository;
        this.farmRepository = farmRepository;
        this.fertilizationMapper = fertilizationMapper;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Create a new fertilization from DTO
     */
    @Override
    @Transactional
    public FertilizationResponseDTO createFertilization(FertilizationRequestDTO dto) {
        Parcel parcel = parcelRepository.findById(dto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + dto.getParcelId()));

        Fertilization fertilization = fertilizationMapper.toEntity(dto);
        fertilization.setParcel(parcel);

        if (fertilization.getStatus() == null) {
            fertilization.setStatus(FertilizationStatus.SCHEDULED);
        }

        Fertilization saved = fertilizationRepository.save(fertilization);
        log.info("Created fertilization ID: {} for parcel: {} at {}",
                saved.getId(), parcel.getName(), saved.getScheduledDatetime());

        return fertilizationMapper.toDTO(saved);
    }

    /**
     * Schedule a new fertilization for a parcel
     */
    @Override
    @Transactional
    public FertilizationResponseDTO scheduleFertilization(Long parcelId, LocalDateTime scheduledDatetime, String fertilizerType) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + parcelId));

        Fertilization fertilization = new Fertilization();
        fertilization.setParcel(parcel);
        fertilization.setScheduledDatetime(scheduledDatetime);
        fertilization.setFertilizerType(fertilizerType);
        fertilization.setStatus(FertilizationStatus.SCHEDULED);

        Fertilization saved = fertilizationRepository.save(fertilization);
        log.info("Scheduled fertilization ID: {} for parcel: {} at {}",
                saved.getId(), parcel.getName(), scheduledDatetime);

        return fertilizationMapper.toDTO(saved);
    }

    /**
     * Mark a fertilization as pending (notification sent to user)
     */
    @Override
    @Transactional
    public void markAsPending(Long fertilizationId) {
        Fertilization fertilization = fertilizationRepository.findById(fertilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + fertilizationId));

        fertilization.setStatus(FertilizationStatus.PENDING);
        fertilization.setUpdatedAt(LocalDateTime.now());

        fertilizationRepository.save(fertilization);
        log.info("Marked fertilization ID: {} as PENDING", fertilizationId);
    }

    /**
     * Mark a fertilization as completed and automatically schedule the next one
     */
    @Override
    @Transactional
    public FertilizationResponseDTO markAsCompleted(Long fertilizationId, String notes) {
        Fertilization fertilization = fertilizationRepository.findById(fertilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + fertilizationId));

        fertilization.setStatus(FertilizationStatus.COMPLETED);
        fertilization.setCompletedDatetime(LocalDateTime.now());
        fertilization.setUpdatedAt(LocalDateTime.now());

        if (notes != null && !notes.isEmpty()) {
            fertilization.setNotes(notes);
        }

        // Update parcel's last fertilized timestamp
        Parcel parcel = fertilization.getParcel();
        LocalDateTime completedAt = LocalDateTime.now();
        parcel.setLastFertilizedAt(completedAt);
        parcelRepository.save(parcel);

        Fertilization updated = fertilizationRepository.save(fertilization);
        log.info("Marked fertilization ID: {} as COMPLETED for parcel: {}",
                fertilizationId, parcel.getName());

        // Send completion notification email
        try {
            emailNotificationService.sendFertilizationCompletedNotification(updated);
        } catch (Exception emailEx) {
            log.error("Failed to send fertilization completed notification email", emailEx);
        }

        // Automatically schedule the next fertilization
        scheduleNextFertilization(parcel, completedAt);

        return fertilizationMapper.toDTO(updated);
    }

    /**
     * Schedule the next fertilization for a parcel based on its crop's fertilization frequency
     */
    private void scheduleNextFertilization(Parcel parcel, LocalDateTime lastFertilizedAt) {
        // Check if parcel has crop with fertilization configuration
        if (parcel.getCrop() == null ||
            parcel.getCrop().getFertilizationFrequencyDays() == null ||
            parcel.getCrop().getFertilizerType() == null) {
            log.debug("Parcel {} does not have fertilization configuration, skipping auto-schedule",
                    parcel.getName());
            return;
        }

        int frequencyDays = parcel.getCrop().getFertilizationFrequencyDays();
        String fertilizerType = parcel.getCrop().getFertilizerType();

        // Calculate next fertilization date
        LocalDateTime nextFertilizationDate = lastFertilizedAt.plusDays(frequencyDays);

        // Create the next fertilization
        Fertilization nextFertilization = new Fertilization();
        nextFertilization.setParcel(parcel);
        nextFertilization.setScheduledDatetime(nextFertilizationDate);
        nextFertilization.setFertilizerType(fertilizerType);
        nextFertilization.setStatus(FertilizationStatus.SCHEDULED);

        fertilizationRepository.save(nextFertilization);

        log.info("Auto-scheduled next fertilization for parcel: {} on {} (frequency: {} days)",
                parcel.getName(), nextFertilizationDate, frequencyDays);
    }

    /**
     * Cancel a fertilization
     */
    @Override
    @Transactional
    public FertilizationResponseDTO cancelFertilization(Long fertilizationId, String notes) {
        Fertilization fertilization = fertilizationRepository.findById(fertilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + fertilizationId));

        fertilization.setStatus(FertilizationStatus.CANCELLED);
        fertilization.setUpdatedAt(LocalDateTime.now());

        if (notes != null && !notes.isEmpty()) {
            fertilization.setNotes(notes);
        }

        Fertilization updated = fertilizationRepository.save(fertilization);
        log.info("Cancelled fertilization ID: {}", fertilizationId);

        // Send cancellation notification email
        try {
            emailNotificationService.sendFertilizationCancelledNotification(updated);
        } catch (Exception emailEx) {
            log.error("Failed to send fertilization cancelled notification email", emailEx);
        }

        return fertilizationMapper.toDTO(updated);
    }

    /**
     * Get all fertilizations for a parcel
     */
    @Override
    public List<FertilizationResponseDTO> getFertilizationsByParcel(Long parcelId) {
        return fertilizationRepository.findByParcelId(parcelId).stream()
                .map(fertilizationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get fertilizations by status
     */
    @Override
    public List<FertilizationResponseDTO> getFertilizationsByStatus(FertilizationStatus status) {
        return fertilizationRepository.findByStatus(status).stream()
                .map(fertilizationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get fertilizations by status for a specific user
     */
    @Override
    public List<FertilizationResponseDTO> getFertilizationsByStatusForUser(FertilizationStatus status, User user) {
        // Get all farm IDs for the user
        List<Long> farmIds = farmRepository.findByUserId(user.getId()).stream()
                .map(Farm::getId)
                .collect(Collectors.toList());

        if (farmIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all parcel IDs from user's farms
        List<Long> parcelIds = parcelRepository.findByFarmIdIn(farmIds).stream()
                .map(Parcel::getId)
                .collect(Collectors.toList());

        if (parcelIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Get fertilizations by status for user's parcels
        return fertilizationRepository.findByStatusAndParcelIdIn(status, parcelIds).stream()
                .map(fertilizationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get fertilizations that are due before a certain date/time and have a specific status
     */
    @Override
    public List<Fertilization> getFertilizationsDueBeforeByStatus(LocalDateTime dateTime, FertilizationStatus status) {
        return fertilizationRepository.findByStatusAndScheduledDatetimeBefore(status, dateTime);
    }

    /**
     * Get a fertilization by ID
     */
    @Override
    public FertilizationResponseDTO getFertilizationById(Long id) {
        Fertilization fertilization = fertilizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + id));
        return fertilizationMapper.toDTO(fertilization);
    }

    /**
     * Update fertilization status
     */
    @Override
    @Transactional
    public FertilizationResponseDTO updateFertilizationStatus(Long fertilizationId, FertilizationStatus status) {
        Fertilization fertilization = fertilizationRepository.findById(fertilizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + fertilizationId));

        fertilization.setStatus(status);
        fertilization.setUpdatedAt(LocalDateTime.now());

        Fertilization updated = fertilizationRepository.save(fertilization);
        return fertilizationMapper.toDTO(updated);
    }

    /**
     * Update a fertilization
     */
    @Override
    @Transactional
    public FertilizationResponseDTO updateFertilization(Long id, FertilizationRequestDTO dto) {
        Fertilization fertilization = fertilizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fertilization not found with id: " + id));

        fertilizationMapper.updateEntity(dto, fertilization);
        fertilization.setUpdatedAt(LocalDateTime.now());

        Fertilization updated = fertilizationRepository.save(fertilization);
        log.info("Updated fertilization ID: {}", id);

        return fertilizationMapper.toDTO(updated);
    }

    /**
     * Delete a fertilization
     */
    @Override
    @Transactional
    public void deleteFertilization(Long id) {
        if (!fertilizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fertilization not found with id: " + id);
        }
        fertilizationRepository.deleteById(id);
        log.info("Deleted fertilization ID: {}", id);
    }
}

