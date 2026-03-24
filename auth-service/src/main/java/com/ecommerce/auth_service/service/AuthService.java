package com.ecommerce.auth_service.service;


import com.ecommerce.auth_service.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);
}