package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.CropRequestDTO;
import com.finki.agrimanagement.dto.response.CropResponseDTO;
import com.finki.agrimanagement.entity.Crop;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.CropMapper;
import com.finki.agrimanagement.repository.CropRepository;
import com.finki.agrimanagement.service.CropService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CropServiceImpl implements CropService {
    private final CropRepository cropRepository;
    private final CropMapper cropMapper;

    public CropServiceImpl(CropRepository cropRepository, CropMapper cropMapper) {
        this.cropRepository = cropRepository;
        this.cropMapper = cropMapper;
    }

    @Override
    @Transactional
    public CropResponseDTO createCrop(CropRequestDTO dto) {
        Crop crop = cropMapper.toEntity(dto);
        Crop saved = cropRepository.save(crop);
        return cropMapper.toDTO(saved);
    }

    @Override
    public List<CropResponseDTO> getAllCrops() {
        return cropRepository.findAll().stream()
                .map(cropMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CropResponseDTO getCropById(Long id) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));
        return cropMapper.toDTO(crop);
    }

    @Override
    @Transactional
    public CropResponseDTO updateCrop(Long id, CropRequestDTO dto) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));
        cropMapper.updateEntity(dto, crop);
        Crop updated = cropRepository.save(crop);
        return cropMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCrop(Long id) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));
        cropRepository.delete(crop);
    }
}

