package com.example.eshop.repository.interfaces;

import com.example.eshop.model.dao.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    // Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}