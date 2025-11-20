package com.finki.agrimanagement.dto.request;

import com.finki.agrimanagement.enums.IrrigationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IrrigationRequestDTO {

    @NotNull(message = "Parcel ID is required")
    private Long parcelId;

    @NotNull(message = "Scheduled datetime is required")
    @Future(message = "Scheduled datetime must be in the future")
    private LocalDateTime scheduledDatetime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @DecimalMin(value = "0.01", message = "Water amount must be positive")
    private Double waterAmountLiters;

    @NotNull(message = "Status is required")
    private IrrigationStatus status;
}

