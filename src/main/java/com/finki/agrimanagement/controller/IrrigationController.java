package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.request.IrrigationRequestDTO;
import com.finki.agrimanagement.dto.response.IrrigationResponseDTO;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.enums.IrrigationStatus;
import com.finki.agrimanagement.service.IrrigationExecutionService;
import com.finki.agrimanagement.service.IrrigationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/irrigations")
public class IrrigationController {

    private final IrrigationService irrigationService;
    private final IrrigationExecutionService irrigationExecutionService;

    public IrrigationController(IrrigationService irrigationService,
                                IrrigationExecutionService irrigationExecutionService) {
        this.irrigationService = irrigationService;
        this.irrigationExecutionService = irrigationExecutionService;
    }

    @PostMapping
    public ResponseEntity<IrrigationResponseDTO> createIrrigation(@Valid @RequestBody IrrigationRequestDTO dto) {
        return new ResponseEntity<>(irrigationService.createIrrigation(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IrrigationResponseDTO>> getAllIrrigations() {
        return ResponseEntity.ok(irrigationService.getAllIrrigations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IrrigationResponseDTO> getIrrigationById(@PathVariable Long id) {
        return ResponseEntity.ok(irrigationService.getIrrigationById(id));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<IrrigationResponseDTO>> getIrrigationsByParcel(@PathVariable Long parcelId) {
        return ResponseEntity.ok(irrigationService.getIrrigationsByParcelId(parcelId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<IrrigationResponseDTO>> getIrrigationsByStatus(@PathVariable IrrigationStatus status) {
        return ResponseEntity.ok(irrigationService.getIrrigationsByStatus(status));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<IrrigationResponseDTO>> getUpcomingIrrigations() {
        return ResponseEntity.ok(irrigationService.getUpcomingIrrigations());
    }

    @GetMapping("/user/upcoming")
    public ResponseEntity<List<IrrigationResponseDTO>> getUpcomingIrrigationsForUser(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(irrigationService.getUpcomingIrrigationsForUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IrrigationResponseDTO> updateIrrigation(@PathVariable Long id,
                                                                   @Valid @RequestBody IrrigationRequestDTO dto) {
        return ResponseEntity.ok(irrigationService.updateIrrigation(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IrrigationResponseDTO> updateStatus(@PathVariable Long id,
                                                               @RequestParam IrrigationStatus status) {
        return ResponseEntity.ok(irrigationService.updateIrrigationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIrrigation(@PathVariable Long id) {
        irrigationService.deleteIrrigation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<String> executeIrrigation(@PathVariable Long id) {
        irrigationExecutionService.executeIrrigation(id);
        return ResponseEntity.ok("Irrigation executed successfully");
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stopIrrigation(@PathVariable Long id) {
        irrigationExecutionService.stopIrrigation(id);
        return ResponseEntity.ok("Irrigation stopped successfully");
    }
}

