package com.finki.agrimanagement.dto.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentWeatherResponseDTO {
    private CoordDTO coord;
    private List<WeatherConditionDTO> weather;
    private String base;
    private MainWeatherDTO main;
    private Integer visibility;
    private WindDTO wind;
    private RainDTO rain;
    private CloudsDTO clouds;
    private Long dt;
    private SysDTO sys;
    private Integer timezone;
    private Integer id;
    private String name;
    private Integer cod;
}

