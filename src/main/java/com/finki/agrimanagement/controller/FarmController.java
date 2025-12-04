package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.dto.request.FarmRequestDTO;
import com.finki.agrimanagement.dto.response.FarmResponseDTO;
import com.finki.agrimanagement.entity.User;
import com.finki.agrimanagement.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping
    public ResponseEntity<FarmResponseDTO> createFarm(@Valid @RequestBody FarmRequestDTO dto,
                                                       @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(farmService.createFarm(dto, user), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FarmResponseDTO>> getAllFarms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(farmService.getAllFarms(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmResponseDTO> getFarmById(@PathVariable Long id,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(farmService.getFarmById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FarmResponseDTO> updateFarm(@PathVariable Long id,
                                                      @Valid @RequestBody FarmRequestDTO dto,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(farmService.updateFarm(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@PathVariable Long id,
                                           @AuthenticationPrincipal User user) {
        farmService.deleteFarm(id, user);
        return ResponseEntity.noContent().build();
    }
}

