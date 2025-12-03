package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    void deleteUser(Long id);

    UserResponseDTO toggleUserEnabled(Long id);
}

