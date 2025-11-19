package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.request.FarmRequestDTO;
import com.finki.agrimanagement.dto.response.FarmResponseDTO;
import com.finki.agrimanagement.entity.Farm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FarmMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    Farm toEntity(FarmRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    void updateEntity(FarmRequestDTO requestDTO, @MappingTarget Farm farm);

    @Mapping(target = "parcelCount", source = "parcelCount")
    FarmResponseDTO toDTO(Farm farm);
}
