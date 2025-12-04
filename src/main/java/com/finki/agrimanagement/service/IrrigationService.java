package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.request.IrrigationRequestDTO;
import com.finki.agrimanagement.dto.response.IrrigationResponseDTO;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.IrrigationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IrrigationService {

    IrrigationResponseDTO createIrrigation(IrrigationRequestDTO dto);

    List<IrrigationResponseDTO> getAllIrrigations();

    IrrigationResponseDTO getIrrigationById(Long id);

    IrrigationResponseDTO updateIrrigation(Long id, IrrigationRequestDTO dto);

    void deleteIrrigation(Long id);

    List<IrrigationResponseDTO> getIrrigationsByParcelId(Long parcelId);

    List<IrrigationResponseDTO> getIrrigationsByStatus(IrrigationStatus status);

    List<IrrigationResponseDTO> getUpcomingIrrigations();

    List<IrrigationResponseDTO> getUpcomingIrrigationsForUser(User user);

    IrrigationResponseDTO updateIrrigationStatus(Long id, IrrigationStatus newStatus);

    List<Irrigation> getIrrigationsDueBeforeByStatus(LocalDateTime dateTime, IrrigationStatus status);
}

