package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.IrrigationRequestDTO;
import com.finki.agrimanagement.dto.response.IrrigationResponseDTO;
import com.finki.agrimanagement.entity.Farm;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.IrrigationStatus;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.IrrigationMapper;
import com.finki.agrimanagement.repository.FarmRepository;
import com.finki.agrimanagement.repository.IrrigationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.IrrigationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IrrigationServiceImpl implements IrrigationService {

    private final IrrigationRepository irrigationRepository;
    private final ParcelRepository parcelRepository;
    private final FarmRepository farmRepository;
    private final IrrigationMapper irrigationMapper;

    public IrrigationServiceImpl(IrrigationRepository irrigationRepository,
                                 ParcelRepository parcelRepository,
                                 FarmRepository farmRepository,
                                 IrrigationMapper irrigationMapper) {
        this.irrigationRepository = irrigationRepository;
        this.parcelRepository = parcelRepository;
        this.farmRepository = farmRepository;
        this.irrigationMapper = irrigationMapper;
    }

    @Override
    @Transactional
    public IrrigationResponseDTO createIrrigation(IrrigationRequestDTO dto) {
        Irrigation irrigation = irrigationMapper.toEntity(dto);

        Parcel parcel = parcelRepository.findById(dto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + dto.getParcelId()));
        irrigation.setParcel(parcel);

        irrigation.setCreatedAt(LocalDateTime.now());
        irrigation.setUpdatedAt(LocalDateTime.now());

        if (irrigation.getStatusDescription() == null || irrigation.getStatusDescription().isEmpty()) {
            irrigation.setStatusDescription(getDefaultStatusDescription(irrigation.getStatus()));
        }

        Irrigation saved = irrigationRepository.save(irrigation);
        return irrigationMapper.toDTO(saved);
    }

    @Override
    public List<IrrigationResponseDTO> getAllIrrigations() {
        return irrigationRepository.findAll().stream()
                .sorted(Comparator.comparingInt((Irrigation i) -> getStatusPriority(i.getStatus()))
                        .thenComparing(Irrigation::getScheduledDatetime))
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public IrrigationResponseDTO getIrrigationById(Long id) {
        Irrigation irrigation = irrigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Irrigation not found with id: " + id));
        return irrigationMapper.toDTO(irrigation);
    }

    @Override
    @Transactional
    public IrrigationResponseDTO updateIrrigation(Long id, IrrigationRequestDTO dto) {
        Irrigation irrigation = irrigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Irrigation not found with id: " + id));

        irrigationMapper.updateEntity(dto, irrigation);

        Parcel parcel = parcelRepository.findById(dto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + dto.getParcelId()));
        irrigation.setParcel(parcel);

        irrigation.setUpdatedAt(LocalDateTime.now());

        if (irrigation.getStatusDescription() == null || irrigation.getStatusDescription().isEmpty()) {
            irrigation.setStatusDescription(getDefaultStatusDescription(irrigation.getStatus()));
        }

        Irrigation updated = irrigationRepository.save(irrigation);
        return irrigationMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteIrrigation(Long id) {
        if (!irrigationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Irrigation not found with id: " + id);
        }
        irrigationRepository.deleteById(id);
    }

    @Override
    public List<IrrigationResponseDTO> getIrrigationsByParcelId(Long parcelId) {
        return irrigationRepository.findByParcelId(parcelId).stream()
                .sorted(Comparator.comparingInt((Irrigation i) -> getStatusPriority(i.getStatus()))
                        .thenComparing(Irrigation::getScheduledDatetime))
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrrigationResponseDTO> getIrrigationsByStatus(IrrigationStatus status) {
        return irrigationRepository.findByStatus(status).stream()
                .sorted(Comparator.comparing(Irrigation::getScheduledDatetime))
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrrigationResponseDTO> getUpcomingIrrigations() {
        return irrigationRepository.findByStatusAndScheduledDatetimeAfter(
                        IrrigationStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .sorted(Comparator.comparing(Irrigation::getScheduledDatetime))
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrrigationResponseDTO> getUpcomingIrrigationsForUser(User user) {
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

        // Get upcoming irrigations for user's parcels
        return irrigationRepository.findByStatusAndScheduledDatetimeAfterAndParcelIdIn(
                        IrrigationStatus.SCHEDULED,
                        LocalDateTime.now(),
                        parcelIds)
                .stream()
                .sorted(Comparator.comparing(Irrigation::getScheduledDatetime))
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IrrigationResponseDTO updateIrrigationStatus(Long id, IrrigationStatus newStatus) {
        Irrigation irrigation = irrigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Irrigation not found with id: " + id));

        irrigation.setStatus(newStatus);
        irrigation.setUpdatedAt(LocalDateTime.now());
        irrigation.setStatusDescription(getDefaultStatusDescription(newStatus));

        Irrigation updated = irrigationRepository.save(irrigation);
        return irrigationMapper.toDTO(updated);
    }

    @Override
    public List<Irrigation> getIrrigationsDueBeforeByStatus(LocalDateTime dateTime, IrrigationStatus status) {
        return irrigationRepository.findByStatusAndScheduledDatetimeBefore(status, dateTime);
    }

    /**
     * Get default status description based on irrigation status
     */
    private String getDefaultStatusDescription(IrrigationStatus status) {
        return switch (status) {
            case SCHEDULED -> "Irrigation scheduled and waiting to be executed";
            case IN_PROGRESS -> "Irrigation is currently in progress";
            case COMPLETED -> "Irrigation completed successfully";
            case CANCELLED -> "Irrigation was cancelled";
            case FAILED -> "Irrigation execution failed";
            case RETRYING -> "Irrigation retry after a failure";
            case STOPPED -> "Irrigation was stopped manually";
        };
    }

    /**
     * Get priority for irrigation status
     */
    private int getStatusPriority(IrrigationStatus status) {
        return switch (status) {
            case IN_PROGRESS -> 1;
            case RETRYING -> 2;
            case SCHEDULED -> 3;
            case FAILED -> 4;
            case STOPPED -> 5;
            case CANCELLED -> 6;
            case COMPLETED -> 7;
        };
    }
}
