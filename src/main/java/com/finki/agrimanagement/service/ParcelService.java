package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.request.ParcelRequestDTO;
import com.finki.agrimanagement.dto.response.ParcelResponseDTO;
import com.finki.agrimanagement.dto.weather.ParcelWeatherDTO;

import java.util.List;

public interface ParcelService {

    ParcelResponseDTO createParcel(ParcelRequestDTO dto);

    List<ParcelResponseDTO> getAllParcels();

    ParcelResponseDTO getParcelById(Long id);

    ParcelResponseDTO updateParcel(Long id, ParcelRequestDTO dto);

    void deleteParcel(Long id);

    List<ParcelResponseDTO> getParcelsByFarmId(Long farmId);

    List<ParcelResponseDTO> getParcelsByCropId(Long cropId);

    ParcelWeatherDTO getParcelWeather(Long parcelId);
}

