package com.example.meustudio.auth.CreateUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "User é obrigatório!")
    @Size(max = 14, message = "User deve ter no máximo 14 caracteres")
    String username,

    @NotBlank(message = "Senha é obrigatório!")
    @Size(max = 20, message = "Senha deve ter no máximo 20 caracteres")
    String password,

    @NotBlank(message =" E-mail é obrigatório")
    @Email
    String email,

    @NotBlank(message = "Telefone é obrigatorio")
    String telefone
){}
