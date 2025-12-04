package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.request.FertilizationRequestDTO;
import com.finki.agrimanagement.dto.response.FertilizationResponseDTO;
import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.FertilizationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface FertilizationService {

    FertilizationResponseDTO createFertilization(FertilizationRequestDTO dto);

    FertilizationResponseDTO scheduleFertilization(Long parcelId, LocalDateTime scheduledDatetime, String fertilizerType);

    void markAsPending(Long fertilizationId);

    FertilizationResponseDTO markAsCompleted(Long fertilizationId, String notes);

    FertilizationResponseDTO cancelFertilization(Long fertilizationId, String notes);

    List<FertilizationResponseDTO> getFertilizationsByParcel(Long parcelId);

    List<FertilizationResponseDTO> getFertilizationsByStatus(FertilizationStatus status);

    List<FertilizationResponseDTO> getFertilizationsByStatusForUser(FertilizationStatus status, User user);

    List<Fertilization> getFertilizationsDueBeforeByStatus(LocalDateTime dateTime, FertilizationStatus status);

    FertilizationResponseDTO getFertilizationById(Long id);

    FertilizationResponseDTO updateFertilizationStatus(Long fertilizationId, FertilizationStatus status);

    FertilizationResponseDTO updateFertilization(Long id, FertilizationRequestDTO dto);

    void deleteFertilization(Long id);
}

