package com.example.meustudio.Auth;


import java.util.UUID;
public record UserResponse(
        String username,
        String email
){
    public static UserResponse fromEntity(User user){
        return new UserResponse(
                user.getUsername(),
                user.getEmail()
        );
    }
}