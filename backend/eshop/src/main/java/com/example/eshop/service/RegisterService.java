package com.example.eshop.service;

import com.example.eshop.exception.EmailAlreadyInUseException;
import com.example.eshop.model.User;
import com.example.eshop.model.common.Role;
import com.example.eshop.model.common.UserStatus;
import com.example.eshop.model.dto.auth.RegisterRequest;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserJpaRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public User registerCustomer(RegisterRequest dto) {
        return register(dto);
    }

    private User register(RegisterRequest dto){
        if (userRepo.existsByEmail(dto.email())) {
            throw new EmailAlreadyInUseException(dto.email());
        }

        User user = User.builder()
                .email(dto.email())
                .hashedPassword(passwordEncoder.encode(dto.passwordFields().password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .phoneNumber(dto.phone())
                .address(dto.address())
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepo.save(user);
    }
}
