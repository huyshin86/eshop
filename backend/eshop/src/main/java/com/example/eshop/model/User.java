package com.example.eshop.model;

import com.example.eshop.model.common.Role;
import com.example.eshop.model.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/*
    This entity has complex business behaviors so it need to be separated from JPA entity.
*/

@Getter
@AllArgsConstructor
public class User {
    private final Long id;
    private String email;
    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Role role;
    private UserStatus status;
    private Instant deletedAt;
    private List<CartItem> cartItems = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();

    // For create a new user from request
    public User(String email, String hashedPassword, String firstName, String lastName, String phoneNumber, String address, Role role){
        if(email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email can not be empty!");
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) throw new IllegalArgumentException("Password can not be empty!");

        this.id = null;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    // User basic business logics
    protected void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("New role cannot be null");
        }
        if (newRole.equals(this.role)) {
            throw new IllegalArgumentException("New role cannot be the same as the current role");
        }
        this.role = newRole;
    }

    public boolean isActive() {
        return status.equals(UserStatus.ACTIVE); // Check if the user is active
    }

    public boolean isLocked() {
        return status.equals(UserStatus.LOCKED); // Check if the user is locked
    }

    public boolean isDeleted() {
        return deletedAt != null || status.equals(UserStatus.INACTIVE); // Check if the user is marked as deleted
    }

    protected void setHashedPassword(String newHashedPassword) {
        if (newHashedPassword == null || newHashedPassword.trim().isEmpty()) throw new IllegalArgumentException("New password can not be empty!");
        this.hashedPassword = newHashedPassword;
    }

    protected void lock() {
        if (isLocked()) {
            throw new IllegalStateException("User is already locked");
        }
        if (isDeleted()) {
            throw new IllegalStateException("User is inactive and cannot be locked");
        }
        this.status = UserStatus.LOCKED; // Set status to locked
    }

    protected void unlock() {
        if (isActive()) {
            throw new IllegalStateException("User is already active");
        }
        if (isDeleted()) {
            throw new IllegalStateException("User is inactive and cannot be unlocked");
        }
        this.status = UserStatus.ACTIVE; // Set status to active
    }

    public void delete() {
        if (isLocked()) {
            throw new IllegalStateException("User is locked and cannot be deleted");
        }
        if (isDeleted()) {
            throw new IllegalStateException("User is already inactive");
        }
        this.deletedAt = Instant.now();// Update deleted time to now
        this.status = UserStatus.INACTIVE; // Set status to inactive
    }

    public void restore() {
        if (isActive()) {
            throw new IllegalStateException("User is already active");
        }
        if (isLocked()) {
            throw new IllegalStateException("User is locked and cannot be restored");
        }
        this.deletedAt = null; // Clear deleted time
        this.status = UserStatus.ACTIVE; // Restore status to active
    }
}
