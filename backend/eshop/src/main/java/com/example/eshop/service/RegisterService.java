package com.example.eshop.service;

import com.example.eshop.exception.EmailAlreadyInUseException;
import com.example.eshop.model.User;
import com.example.eshop.model.common.Role;
import com.example.eshop.model.dto.auth.RegisterRequest;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerCustomer(RegisterRequest dto) {
        return register(dto);
    }

    private User register(RegisterRequest dto){
        if (userJpaRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyInUseException(dto.email());
        }

        User user = new User(
                dto.email(),
                passwordEncoder.encode(dto.passwordFields().password()),
                dto.firstName(),
                dto.lastName(),
                dto.phone(),
                dto.address(),
                Role.CUSTOMER);

        return userJpaRepository.save(user);
    }
}
