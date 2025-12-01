package com.finki.agrimanagement.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParcelWeatherDTO {
    // Parcel information
    private Long parcelId;
    private String parcelName;
    private Double latitude;
    private Double longitude;

    // Current conditions
    private String weatherCondition;        // e.g., "Rain", "Clear", "Clouds"
    private String weatherDescription;      // e.g., "light rain", "clear sky"
    private String weatherIcon;             // Icon code from API

    // Temperature (in Celsius)
    private Double temperature;
    private Double feelsLike;

    // Atmospheric conditions
    private Integer humidity;               // Percentage
    private Integer pressure;               // hPa

    // Wind
    private Double windSpeed;               // m/s

    // Rain
    private Double rainExpectedInOneHour;   // mm expected in next hour (0 if no rain expected)

    // Cloud coverage
    private Integer cloudiness;             // Percentage

    // Visibility
    private Integer visibility;             // Meters

    // Location name from weather API
    private String locationName;
    private String country;
}

