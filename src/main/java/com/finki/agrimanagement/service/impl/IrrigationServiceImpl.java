package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.IrrigationRequestDTO;
import com.finki.agrimanagement.dto.response.IrrigationResponseDTO;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.enums.IrrigationStatus;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.IrrigationMapper;
import com.finki.agrimanagement.repository.IrrigationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.IrrigationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IrrigationServiceImpl implements IrrigationService {

    private final IrrigationRepository irrigationRepository;
    private final ParcelRepository parcelRepository;
    private final IrrigationMapper irrigationMapper;

    public IrrigationServiceImpl(IrrigationRepository irrigationRepository,
                                 ParcelRepository parcelRepository,
                                 IrrigationMapper irrigationMapper) {
        this.irrigationRepository = irrigationRepository;
        this.parcelRepository = parcelRepository;
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

        Irrigation saved = irrigationRepository.save(irrigation);
        return irrigationMapper.toDTO(saved);
    }

    @Override
    public List<IrrigationResponseDTO> getAllIrrigations() {
        return irrigationRepository.findAll().stream()
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
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrrigationResponseDTO> getIrrigationsByStatus(IrrigationStatus status) {
        return irrigationRepository.findByStatus(status).stream()
                .map(irrigationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IrrigationResponseDTO> getUpcomingIrrigations() {
        return irrigationRepository.findByStatusAndScheduledDatetimeAfter(
                        IrrigationStatus.SCHEDULED, LocalDateTime.now())
                .stream()
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

        Irrigation updated = irrigationRepository.save(irrigation);
        return irrigationMapper.toDTO(updated);
    }
}
