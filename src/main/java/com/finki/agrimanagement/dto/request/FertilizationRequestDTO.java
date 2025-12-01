package com.finki.agrimanagement.dto.request;

import com.finki.agrimanagement.enums.FertilizationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FertilizationRequestDTO {

    @NotNull(message = "Parcel ID is required")
    private Long parcelId;

    @NotNull(message = "Scheduled datetime is required")
    private LocalDateTime scheduledDatetime;

    private String fertilizerType;

    private FertilizationStatus status;

    private String notes;
}
