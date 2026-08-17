package com.example.meustudio.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.meustudio.auth.CreateUser.CreateUserRequest;
import com.example.meustudio.auth.CreateUser.CreateUserResponse;
import com.example.meustudio.auth.Login.LoginRequest;
import com.example.meustudio.config.Encoder;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Encoder encoder;

    public UserService(UserRepository userRepository, Encoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public CreateUserResponse criar(CreateUserRequest userRequest) {

        String senhaHash = encoder.passwordEncoder().encode(userRequest.password());

        User user = new User();
        user.setUsername(userRequest.username());
        user.setPassword(senhaHash);
        user.setEmail(userRequest.email());
        user.setTelefone(userRequest.telefone());

        return CreateUserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public User autenticar(LoginRequest userRequest) {
        User user = userRepository.findByUsername(userRequest.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User ou senha inválidos"));

        boolean senhaCorreta = encoder.passwordEncoder()
                .matches(userRequest.password(), user.getPassword());

        if (!senhaCorreta) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User ou senha inválidos");
        }

        return user;
    }

}
