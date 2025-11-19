package com.finki.agrimanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FarmRequestDTO {

    @NotBlank(message = "Farm name is required")
    @Size(min = 2, max = 255, message = "Farm name must be between 2 and 255 characters")
    private String name;

    private String location;

}
