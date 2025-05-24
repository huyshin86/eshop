package com.example.eshop.repository.implement;

import com.example.eshop.model.User;
import com.example.eshop.model.dao.UserEntity;
import com.example.eshop.model.mapper.UserMapper;
import com.example.eshop.repository.interfaces.UserJpaRepository;
import com.example.eshop.repository.interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository repo;
    private final UserMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id)
                .map(mapper::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email)
                .map(mapper::toUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        if (user == null){
            throw new IllegalArgumentException("User can not be null!");
        }
        UserEntity e = mapper.toEntity(user);
        UserEntity saved = repo.save(e);
        return mapper.toUser(saved);
    }

    @Override
    public void delete(User user) {
        repo.deleteById(user.getId());
    }
}
