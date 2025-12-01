package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.weather.CurrentWeatherResponseDTO;
import com.finki.agrimanagement.dto.weather.RainCheckResultDTO;

public interface WeatherService {

    /**
     * Check rain conditions at the location
     * Returns information about current rain and expected rain in next hour
     *
     * @param latitude  Parcel latitude
     * @param longitude Parcel longitude
     * @return RainCheckResultDTO with detailed rain information
     */
    RainCheckResultDTO checkRainConditions(Double latitude, Double longitude);

    /**
     * Get current weather from OpenWeather Current Weather API
     *
     * @param latitude  Parcel latitude
     * @param longitude Parcel longitude
     * @return Current weather data
     */
    CurrentWeatherResponseDTO getCurrentWeather(Double latitude, Double longitude);
}

