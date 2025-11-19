package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.ParcelRequestDTO;
import com.finki.agrimanagement.dto.response.ParcelResponseDTO;
import com.finki.agrimanagement.entity.Crop;
import com.finki.agrimanagement.entity.Farm;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.ParcelMapper;
import com.finki.agrimanagement.repository.CropRepository;
import com.finki.agrimanagement.repository.FarmRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.ParcelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ParcelServiceImpl implements ParcelService {
    private final ParcelRepository parcelRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final ParcelMapper parcelMapper;

    public ParcelServiceImpl(ParcelRepository parcelRepository,
                             FarmRepository farmRepository,
                             CropRepository cropRepository,
                             ParcelMapper parcelMapper) {
        this.parcelRepository = parcelRepository;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.parcelMapper = parcelMapper;
    }

    @Override
    @Transactional
    public ParcelResponseDTO createParcel(ParcelRequestDTO dto) {
        Parcel parcel = parcelMapper.toEntity(dto);

        Farm farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + dto.getFarmId()));
        parcel.setFarm(farm);

        if (dto.getCropId() != null) {
            Crop crop = cropRepository.findById(dto.getCropId())
                    .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + dto.getCropId()));
            parcel.setCrop(crop);
        }

        Parcel saved = parcelRepository.save(parcel);
        return parcelMapper.toDTO(saved);
    }

    @Override
    public List<ParcelResponseDTO> getAllParcels() {
        return parcelRepository.findAll().stream()
                .map(parcelMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ParcelResponseDTO getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        return parcelMapper.toDTO(parcel);
    }

    @Override
    @Transactional
    public ParcelResponseDTO updateParcel(Long id, ParcelRequestDTO dto) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));

        parcelMapper.updateEntity(dto, parcel);

        Farm farm = farmRepository.findById(dto.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + dto.getFarmId()));
        parcel.setFarm(farm);

        if (dto.getCropId() != null) {
            Crop crop = cropRepository.findById(dto.getCropId())
                    .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + dto.getCropId()));
            parcel.setCrop(crop);
        } else {
            parcel.setCrop(null);
        }

        Parcel updated = parcelRepository.save(parcel);
        return parcelMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteParcel(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        parcelRepository.delete(parcel);
    }

    @Override
    public List<ParcelResponseDTO> getParcelsByFarmId(Long farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new ResourceNotFoundException("Farm not found with id: " + farmId);
        }
        return parcelRepository.findByFarmId(farmId).stream()
                .map(parcelMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParcelResponseDTO> getParcelsByCropId(Long cropId) {
        if (!cropRepository.existsById(cropId)) {
            throw new ResourceNotFoundException("Crop not found with id: " + cropId);
        }
        return parcelRepository.findByCropId(cropId).stream()
                .map(parcelMapper::toDTO)
                .collect(Collectors.toList());
    }
}

