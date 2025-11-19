package com.finki.agrimanagement.service;


import com.finki.agrimanagement.dto.request.FarmRequestDTO;
import com.finki.agrimanagement.dto.response.FarmResponseDTO;

import java.util.List;

public interface FarmService {

    FarmResponseDTO createFarm(FarmRequestDTO dto);

    List<FarmResponseDTO> getAllFarms();

    FarmResponseDTO getFarmById(Long id);

    FarmResponseDTO updateFarm(Long id, FarmRequestDTO dto);

    void deleteFarm(Long id);
}

