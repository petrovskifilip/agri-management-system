package com.finki.agrimanagement.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

@Data
public class ParcelRequestDTO {

    @NotBlank(message = "Parcel name is required")
    @Size(min = 2, max = 100, message = "Parcel name must be between 2 and 100 characters")
    private String name;

    private Double latitude;

    private Double longitude;

    @DecimalMin(value = "0.01", message = "Area must be greater than 0")
    private Double area;

    private Long farmId;
    @NotNull(message = "Farm ID is required")

    private Long cropId;

}
