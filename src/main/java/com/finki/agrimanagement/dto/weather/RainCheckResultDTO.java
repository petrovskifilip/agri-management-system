package com.finki.agrimanagement.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RainCheckResultDTO {
    private Double latitude;
    private Double longitude;
    private boolean isRaining;
    private boolean rainExpectedInOneHour;
    private String recommendation;
    private String details;
}

