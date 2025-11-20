package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.request.IrrigationRequestDTO;
import com.finki.agrimanagement.dto.response.IrrigationResponseDTO;
import com.finki.agrimanagement.entity.Irrigation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IrrigationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Irrigation toEntity(IrrigationRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(IrrigationRequestDTO requestDTO, @MappingTarget Irrigation irrigation);

    @Mapping(target = "parcelId", source = "parcel.id")
    @Mapping(target = "parcelName", source = "parcel.name")
    IrrigationResponseDTO toDTO(Irrigation irrigation);
}

