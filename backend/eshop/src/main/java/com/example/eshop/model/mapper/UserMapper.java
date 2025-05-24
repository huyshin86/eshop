package com.example.eshop.model.mapper;

import com.example.eshop.model.User;
import com.example.eshop.model.dao.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public User toUser(UserEntity e){
        if (e == null) {
            return null;
        }

        return new User(
                e.getId(),
                e.getEmail(),
                e.getHashedPassword(),
                e.getFirstName(),
                e.getLastName(),
                e.getPhoneNumber(),
                e.getAddress(),
                e.getRole(),
                e.getStatus(),
                e.getDeletedAt(),
                e.getCartItems(),
                e.getOrders());
    }

    public UserEntity toEntity(User u){
        if (u == null){
            return null;
        }

        return new UserEntity(
                u.getId(),
                u.getEmail(),
                u.getHashedPassword(),
                u.getFirstName(),
                u.getLastName(),
                u.getPhoneNumber(),
                u.getAddress(),
                u.getRole(),
                u.getStatus(),
                null,
                null,
                u.getDeletedAt(),
                u.getCartItems(),
                u.getOrders());
    }
}
