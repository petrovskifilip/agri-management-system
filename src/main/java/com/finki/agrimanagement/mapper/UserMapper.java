package com.finki.agrimanagement.mapper;

import com.finki.agrimanagement.dto.response.UserResponseDTO;
import com.finki.agrimanagement.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}

