package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.FarmRequestDTO;
import com.finki.agrimanagement.dto.response.FarmResponseDTO;
import com.finki.agrimanagement.entity.Farm;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.FarmMapper;
import com.finki.agrimanagement.repository.FarmRepository;
import com.finki.agrimanagement.service.FarmService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FarmServiceImpl implements FarmService {
    private final FarmRepository farmRepository;
    private final FarmMapper farmMapper;

    public FarmServiceImpl(FarmRepository farmRepository, FarmMapper farmMapper) {
        this.farmRepository = farmRepository;
        this.farmMapper = farmMapper;
    }

    @Override
    @Transactional
    public FarmResponseDTO createFarm(FarmRequestDTO dto) {
        Farm farm = farmMapper.toEntity(dto);
        Farm saved = farmRepository.save(farm);
        return farmMapper.toDTO(saved);
    }

    @Override
    public List<FarmResponseDTO> getAllFarms() {
        return farmRepository.findAll().stream()
                .map(farmMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FarmResponseDTO getFarmById(Long id) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
        return farmMapper.toDTO(farm);
    }

    @Override
    @Transactional
    public FarmResponseDTO updateFarm(Long id, FarmRequestDTO dto) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
        farmMapper.updateEntity(dto, farm);
        Farm updated = farmRepository.save(farm);
        return farmMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFarm(Long id) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
        farmRepository.delete(farm);
    }
}
