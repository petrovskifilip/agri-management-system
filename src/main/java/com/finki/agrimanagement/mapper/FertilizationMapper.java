package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.request.FertilizationRequestDTO;
import com.finki.agrimanagement.dto.response.FertilizationResponseDTO;
import com.finki.agrimanagement.entity.Fertilization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FertilizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedDatetime", ignore = true)
    Fertilization toEntity(FertilizationRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedDatetime", ignore = true)
    void updateEntity(FertilizationRequestDTO requestDTO, @MappingTarget Fertilization fertilization);

    @Mapping(target = "parcelId", source = "parcel.id")
    @Mapping(target = "parcelName", source = "parcel.name")
    FertilizationResponseDTO toDTO(Fertilization fertilization);
}

