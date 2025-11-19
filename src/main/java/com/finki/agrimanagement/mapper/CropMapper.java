package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.request.CropRequestDTO;
import com.finki.agrimanagement.dto.response.CropResponseDTO;
import com.finki.agrimanagement.entity.Crop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CropMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    Crop toEntity(CropRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    void updateEntity(CropRequestDTO requestDTO, @MappingTarget Crop crop);

    @Mapping(target = "parcelCount", source = "parcelCount")
    CropResponseDTO toDTO(Crop crop);
}

