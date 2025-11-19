package com.finki.agrimanagement.dto.response;

import lombok.Data;

@Data
public class ParcelResponseDTO {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double area;
    private Long farmId;
    private String farmName;
    private Long cropId;
    private String cropName;

}
