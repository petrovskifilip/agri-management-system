package com.finki.agrimanagement.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CropRequestDTO {

    @NotBlank(message = "Crop name is required")
    @Size(max = 150, message = "Crop name must be up to 150 characters")
    private String name;

    private Integer fertilizationFrequencyDays;

    private String fertilizerType;

    private Integer irrigationFrequencyDays;

    private Integer irrigationDurationMinutes;

    private Double waterRequirementLitersPerSqm;
}
