package com.example.eshop.repository.interfaces;

import com.example.eshop.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    void delete(User user);
}
