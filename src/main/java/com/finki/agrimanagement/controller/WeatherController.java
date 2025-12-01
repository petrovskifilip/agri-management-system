package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.weather.CurrentWeatherResponseDTO;
import com.finki.agrimanagement.dto.weather.RainCheckResultDTO;
import com.finki.agrimanagement.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Test endpoint to check current weather for specific coordinates
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return Current weather data
     */
    @GetMapping("/current")
    public ResponseEntity<CurrentWeatherResponseDTO> getCurrentWeather(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        CurrentWeatherResponseDTO weather = weatherService.getCurrentWeather(lat, lon);
        return ResponseEntity.ok(weather);
    }

    /**
     * Test endpoint to check rain conditions
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return Rain check result with detailed information
     */
    @GetMapping("/check-rain")
    public ResponseEntity<RainCheckResultDTO> checkRainConditions(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        RainCheckResultDTO result = weatherService.checkRainConditions(lat, lon);
        return ResponseEntity.ok(result);
    }
}



