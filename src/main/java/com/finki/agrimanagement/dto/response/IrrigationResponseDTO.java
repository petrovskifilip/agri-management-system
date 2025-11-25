package com.finki.agrimanagement.dto.response;

import com.finki.agrimanagement.enums.IrrigationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IrrigationResponseDTO {

    private Long id;
    private Long parcelId;
    private String parcelName;
    private LocalDateTime scheduledDatetime;
    private Integer durationMinutes;
    private Double waterAmountLiters;
    private IrrigationStatus status;
    private LocalDateTime startDatetime;
    private LocalDateTime finishedDatetime;
    private Integer retryCount;
    private LocalDateTime lastRetryAt;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

