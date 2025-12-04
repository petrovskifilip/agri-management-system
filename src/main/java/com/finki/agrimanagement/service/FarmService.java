package com.finki.agrimanagement.service;


import com.finki.agrimanagement.dto.request.FarmRequestDTO;
import com.finki.agrimanagement.dto.response.FarmResponseDTO;
import com.finki.agrimanagement.entity.User;

import java.util.List;

public interface FarmService {

    FarmResponseDTO createFarm(FarmRequestDTO dto, User user);

    List<FarmResponseDTO> getAllFarms(User user);

    FarmResponseDTO getFarmById(Long id, User user);

    FarmResponseDTO updateFarm(Long id, FarmRequestDTO dto, User user);

    void deleteFarm(Long id, User user);
}

