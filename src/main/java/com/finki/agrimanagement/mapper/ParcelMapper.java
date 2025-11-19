package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.request.ParcelRequestDTO;
import com.finki.agrimanagement.dto.response.ParcelResponseDTO;
import com.finki.agrimanagement.entity.Parcel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "crop", ignore = true)
    Parcel toEntity(ParcelRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "crop", ignore = true)
    void updateEntity(ParcelRequestDTO requestDTO, @MappingTarget Parcel parcel);

    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "farmName", source = "farm.name")
    @Mapping(target = "cropId", source = "crop.id")
    @Mapping(target = "cropName", source = "crop.name")
    ParcelResponseDTO toDTO(Parcel parcel);
}

