package com.example.meustudio.auth;


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