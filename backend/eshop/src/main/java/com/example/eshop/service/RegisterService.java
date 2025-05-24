package com.example.eshop.service;

import com.example.eshop.exception.EmailAlreadyInUseException;
import com.example.eshop.model.User;
import com.example.eshop.model.common.Role;
import com.example.eshop.model.dto.auth.RegisterDto;
import com.example.eshop.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerCustomer(RegisterDto dto) {
        return register(dto);
    }

    private User register(RegisterDto dto){
        if (userRepository.existsByEmail(dto.email())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
            throw new EmailAlreadyInUseException(dto.email());
        }

        User user = new User(
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.firstName(),
                dto.lastName(),
                dto.phone(),
                null,
                Role.CUSTOMER);

        return userRepository.save(user);
    }
}
