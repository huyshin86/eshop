package com.example.eshop.service;

import com.example.eshop.exception.PasswordMismatchException;
import com.example.eshop.model.dto.auth.RegisterDto;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {
    
    public void validatePasswords(RegisterDto dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }
    }
}