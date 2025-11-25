package com.finki.agrimanagement.dto.response;

import lombok.Data;

@Data
public class CropResponseDTO {

    private Long id;
    private String name;
    private Integer irrigationFrequencyDays;
    private Integer fertilizationFrequencyDays;
    private Integer irrigationDurationMinutes;
    private Double waterRequirementLitersPerSqm;
    private int parcelCount;

}
