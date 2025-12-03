package com.finki.agrimanagement.service;

import com.finki.agrimanagement.dto.request.LoginRequestDTO;
import com.finki.agrimanagement.dto.request.RegisterRequestDTO;
import com.finki.agrimanagement.dto.response.AuthResponseDTO;

public interface AuthenticationService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO refreshToken(String refreshToken);
}

