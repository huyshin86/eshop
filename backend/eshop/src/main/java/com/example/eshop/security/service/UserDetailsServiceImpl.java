package com.example.eshop.security.service;

import com.example.eshop.model.User;
import com.example.eshop.repository.interfaces.UserRepository;
import com.example.eshop.security.util.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with email %s not found", email)
                ));

        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().toStringRole());

        // Check account status
        boolean accountNonLocked = !user.isLocked();
        boolean enabled = user.isActive();

        return new CustomUserDetails(
                user.getEmail(),
                user.getHashedPassword(),
                Collections.singletonList(authority),
                user.getId(),
                enabled,
                accountNonLocked);
    }
}