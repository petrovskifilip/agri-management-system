package com.finki.agrimanagement.service;


import com.finki.agrimanagement.dto.request.CropRequestDTO;
import com.finki.agrimanagement.dto.response.CropResponseDTO;

import java.util.List;

public interface CropService {

    CropResponseDTO createCrop(CropRequestDTO dto);

    List<CropResponseDTO> getAllCrops();

    CropResponseDTO getCropById(Long id);

    CropResponseDTO updateCrop(Long id, CropRequestDTO dto);

    void deleteCrop(Long id);
}

