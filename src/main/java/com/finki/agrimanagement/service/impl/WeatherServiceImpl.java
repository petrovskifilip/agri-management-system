package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.dto.weather.CurrentWeatherResponseDTO;
import com.finki.agrimanagement.dto.weather.RainCheckResultDTO;
import com.finki.agrimanagement.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WebClient webClient;
    private final String apiKey;

    public WeatherServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${openweather.api.base-url}") String baseUrl,
            @Value("${openweather.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public RainCheckResultDTO checkRainConditions(Double latitude, Double longitude) {
        try {
            CurrentWeatherResponseDTO weather = getCurrentWeather(latitude, longitude);

            // Check if currently raining (weather condition contains "rain")
            boolean isRaining = weather.getWeather() != null &&
                    weather.getWeather().stream()
                            .anyMatch(w ->
                                (w.getMain() != null && w.getMain().toLowerCase().contains("rain")) ||
                                (w.getDescription() != null && w.getDescription().toLowerCase().contains("rain"))
                            );

            // Check if significant rain expected in next hour (> 0.5mm)
            boolean willRainInOneHour = weather.getRain() != null &&
                    weather.getRain().getOneHour() != null &&
                    weather.getRain().getOneHour() > 0.5;

            String recommendation;
            String details;

            if (isRaining) {
                recommendation = "SKIP_IRRIGATION";
                details = "Currently raining at location";
            } else if (willRainInOneHour) {
                recommendation = "SKIP_IRRIGATION";
                double rainAmount = weather.getRain().getOneHour();
                details = String.format("Significant rain expected in next hour (%.2fmm)", rainAmount);
            } else {
                recommendation = "PROCEED_WITH_IRRIGATION";
                details = "No rain currently and none expected in next hour";
            }

            log.info("Rain check for coordinates ({}, {}): isRaining={}, willRainInOneHour={}, recommendation={}",
                    latitude, longitude, isRaining, willRainInOneHour, recommendation);

            return RainCheckResultDTO.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .isRaining(isRaining)
                    .rainExpectedInOneHour(willRainInOneHour)
                    .recommendation(recommendation)
                    .details(details)
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch weather data for coordinates ({}, {}): {}",
                    latitude, longitude, e.getMessage());

            // On error, recommend proceeding with irrigation
            return RainCheckResultDTO.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .isRaining(false)
                    .rainExpectedInOneHour(false)
                    .recommendation("PROCEED_WITH_IRRIGATION")
                    .details("Weather check failed - proceeding with irrigation")
                    .build();
        }
    }

    @Override
    public CurrentWeatherResponseDTO getCurrentWeather(Double latitude, Double longitude) {
        log.info("Fetching current weather for coordinates: ({}, {})", latitude, longitude);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(CurrentWeatherResponseDTO.class)
                .block();
    }
}

