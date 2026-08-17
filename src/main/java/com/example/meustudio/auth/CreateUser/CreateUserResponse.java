package com.example.meustudio.auth.CreateUser;

import com.example.meustudio.auth.User;

public record CreateUserResponse(
        String username,
        String email) {
    public static CreateUserResponse fromEntity(User user) {
        return new CreateUserResponse(
                user.getUsername(),
                user.getEmail());
    }
}   