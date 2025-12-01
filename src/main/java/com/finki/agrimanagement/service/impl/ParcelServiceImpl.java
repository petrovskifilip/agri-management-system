package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.request.ParcelRequestDTO;
import com.finki.agrimanagement.dto.response.ParcelResponseDTO;
import com.finki.agrimanagement.dto.weather.CurrentWeatherResponseDTO;
import com.finki.agrimanagement.dto.weather.ParcelWeatherDTO;
import com.finki.agrimanagement.entity.Crop;
import com.finki.agrimanagement.entity.Farm;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.exception.MissingCoordinatesException;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.mapper.ParcelMapper;
import com.finki.agrimanagement.repository.CropRepository;
import com.finki.agrimanagement.repository.FarmRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.ParcelService;
import com.finki.agrimanagement.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ParcelServiceImpl implements ParcelService {
    private final ParcelRepository parcelRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final ParcelMapper parcelMapper;
    private final WeatherService weatherService;

    public ParcelServiceImpl(ParcelRepository parcelRepository,
                             FarmRepository farmRepository,
                             CropRepository cropRepository,
                             ParcelMapper parcelMapper,
                             WeatherService weatherService) {
        this.parcelRepository = parcelRepository;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.parcelMapper = parcelMapper;
        this.weatherService = weatherService;
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

    @Override
    public ParcelWeatherDTO getParcelWeather(Long parcelId) {
        // Fetch parcel data
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + parcelId));

        // Validate coordinates
        if (parcel.getLatitude() == null || parcel.getLongitude() == null) {
            throw new MissingCoordinatesException(
                    "Parcel coordinates not set. Please set latitude and longitude for this parcel to view weather data."
            );
        }

        log.info("Fetching weather for parcel {} ({}, {})", parcel.getName(), parcel.getLatitude(), parcel.getLongitude());

        try {
            CurrentWeatherResponseDTO weather = weatherService.getCurrentWeather(parcel.getLatitude(), parcel.getLongitude());

            // Extract weather condition information
            String weatherCondition = null;
            String weatherDescription = null;
            String weatherIcon = null;
            if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
                var condition = weather.getWeather().getFirst();
                weatherCondition = condition.getMain();
                weatherDescription = condition.getDescription();
                weatherIcon = condition.getIcon();
            }

            // Extract rain information - set to 0 if null or no data
            Double rainExpectedInOneHour = 0.0;
            if (weather.getRain() != null && weather.getRain().getOneHour() != null) {
                rainExpectedInOneHour = weather.getRain().getOneHour();
            }

            // Build weather overview DTO
            return ParcelWeatherDTO.builder()
                    .parcelId(parcel.getId())
                    .parcelName(parcel.getName())
                    .latitude(parcel.getLatitude())
                    .longitude(parcel.getLongitude())
                    .weatherCondition(weatherCondition)
                    .weatherDescription(weatherDescription)
                    .weatherIcon(weatherIcon)
                    .temperature(weather.getMain() != null ? weather.getMain().getTemp() : null)
                    .feelsLike(weather.getMain() != null ? weather.getMain().getFeelsLike() : null)
                    .humidity(weather.getMain() != null ? weather.getMain().getHumidity() : null)
                    .pressure(weather.getMain() != null ? weather.getMain().getPressure() : null)
                    .windSpeed(weather.getWind() != null ? weather.getWind().getSpeed() : null)
                    .rainExpectedInOneHour(rainExpectedInOneHour)
                    .cloudiness(weather.getClouds() != null ? weather.getClouds().getAll() : null)
                    .visibility(weather.getVisibility())
                    .locationName(weather.getName())
                    .country(weather.getSys() != null ? weather.getSys().getCountry() : null)
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch weather for parcel {} ({}, {}): {}",
                    parcel.getName(), parcel.getLatitude(), parcel.getLongitude(), e.getMessage());

            return ParcelWeatherDTO.builder()
                    .parcelId(parcel.getId())
                    .parcelName(parcel.getName())
                    .latitude(parcel.getLatitude())
                    .longitude(parcel.getLongitude())
                    .rainExpectedInOneHour(0.0)
                    .build();
        }
    }
}

