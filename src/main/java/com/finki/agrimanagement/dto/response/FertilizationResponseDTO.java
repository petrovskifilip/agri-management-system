package com.finki.agrimanagement.dto.response;

import com.finki.agrimanagement.enums.FertilizationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FertilizationResponseDTO {

    private Long id;
    private Long parcelId;
    private String parcelName;
    private LocalDateTime scheduledDatetime;
    private String fertilizerType;
    private FertilizationStatus status;
    private LocalDateTime completedDatetime;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

