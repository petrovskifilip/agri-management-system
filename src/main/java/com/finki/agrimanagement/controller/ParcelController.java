package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.request.ParcelRequestDTO;
import com.finki.agrimanagement.dto.response.ParcelResponseDTO;
import com.finki.agrimanagement.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ResponseEntity<ParcelResponseDTO> createParcel(@Valid @RequestBody ParcelRequestDTO dto) {
        return new ResponseEntity<>(parcelService.createParcel(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ParcelResponseDTO>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(parcelService.getParcelsByFarmId(farmId));
    }

    @GetMapping("/crop/{cropId}")
    public ResponseEntity<List<ParcelResponseDTO>> getParcelsByCrop(@PathVariable Long cropId) {
        return ResponseEntity.ok(parcelService.getParcelsByCropId(cropId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponseDTO> getParcelById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.getParcelById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponseDTO> updateParcel(@PathVariable Long id,
                                                           @Valid @RequestBody ParcelRequestDTO dto) {
        return ResponseEntity.ok(parcelService.updateParcel(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }
}

