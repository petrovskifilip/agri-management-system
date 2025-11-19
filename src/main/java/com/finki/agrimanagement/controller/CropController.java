package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.request.CropRequestDTO;
import com.finki.agrimanagement.dto.response.CropResponseDTO;
import com.finki.agrimanagement.service.CropService;
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
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    public ResponseEntity<CropResponseDTO> createCrop(@Valid @RequestBody CropRequestDTO dto) {
        return new ResponseEntity<>(cropService.createCrop(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CropResponseDTO>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropResponseDTO> getCropById(@PathVariable Long id) {
        return ResponseEntity.ok(cropService.getCropById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropResponseDTO> updateCrop(@PathVariable Long id,
                                                          @Valid @RequestBody CropRequestDTO dto) {
        return ResponseEntity.ok(cropService.updateCrop(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable Long id) {
        cropService.deleteCrop(id);
        return ResponseEntity.noContent().build();
    }
}

