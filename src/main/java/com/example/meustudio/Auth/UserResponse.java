package com.example.meustudio.Auth;


import java.util.UUID;
public record UserResponse(
        String username
){
    public static UserResponse fromEntity(User user){
        return new UserResponse(
                user.getUsername()
        );
    }
}