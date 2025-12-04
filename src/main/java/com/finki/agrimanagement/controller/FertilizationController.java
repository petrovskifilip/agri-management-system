package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.request.FertilizationRequestDTO;
import com.finki.agrimanagement.dto.response.FertilizationResponseDTO;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.FertilizationStatus;
import com.finki.agrimanagement.service.FertilizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fertilizations")
public class FertilizationController {

    private final FertilizationService fertilizationService;

    public FertilizationController(FertilizationService fertilizationService) {
        this.fertilizationService = fertilizationService;
    }

    @PostMapping
    public ResponseEntity<FertilizationResponseDTO> createFertilization(@Valid @RequestBody FertilizationRequestDTO dto) {
        return new ResponseEntity<>(fertilizationService.createFertilization(dto), HttpStatus.CREATED);
    }

    @PostMapping("/schedule")
    public ResponseEntity<FertilizationResponseDTO> scheduleFertilization(
            @RequestParam Long parcelId,
            @RequestParam LocalDateTime scheduledDatetime,
            @RequestParam String fertilizerType) {
        return new ResponseEntity<>(
                fertilizationService.scheduleFertilization(parcelId, scheduledDatetime, fertilizerType),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<FertilizationResponseDTO>> getAllFertilizations() {
        return ResponseEntity.ok(fertilizationService.getAllFertilizations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FertilizationResponseDTO> getFertilizationById(@PathVariable Long id) {
        return ResponseEntity.ok(fertilizationService.getFertilizationById(id));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<FertilizationResponseDTO>> getFertilizationsByParcel(@PathVariable Long parcelId) {
        return ResponseEntity.ok(fertilizationService.getFertilizationsByParcel(parcelId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FertilizationResponseDTO>> getFertilizationsByStatus(@PathVariable FertilizationStatus status) {
        return ResponseEntity.ok(fertilizationService.getFertilizationsByStatus(status));
    }

    @GetMapping("/user/status/{status}")
    public ResponseEntity<List<FertilizationResponseDTO>> getFertilizationsByStatusForUser(
            @PathVariable FertilizationStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(fertilizationService.getFertilizationsByStatusForUser(status, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FertilizationResponseDTO> updateFertilization(
            @PathVariable Long id,
            @Valid @RequestBody FertilizationRequestDTO dto) {
        return ResponseEntity.ok(fertilizationService.updateFertilization(id, dto));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<FertilizationResponseDTO> completeFertilization(
            @PathVariable Long id,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(fertilizationService.markAsCompleted(id, notes));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FertilizationResponseDTO> cancelFertilization(
            @PathVariable Long id,
            @RequestBody(required = false) String notes) {
        return ResponseEntity.ok(fertilizationService.cancelFertilization(id, notes));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FertilizationResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam FertilizationStatus status) {
        return ResponseEntity.ok(fertilizationService.updateFertilizationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFertilization(@PathVariable Long id) {
        fertilizationService.deleteFertilization(id);
        return ResponseEntity.noContent().build();
    }
}

