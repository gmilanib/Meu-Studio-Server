package com.example.meustudio.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meustudio.auth.CreateUser.CreateUserRequest;
import com.example.meustudio.auth.CreateUser.CreateUserResponse;
import com.example.meustudio.auth.Login.LoginRequest;
import com.example.meustudio.auth.Login.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @CrossOrigin("*")
    @PostMapping("/CreateUser")
    public ResponseEntity<CreateUserResponse> criar(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @CrossOrigin("*")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticarUser(@Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        User user = userService.autenticar(request);

        sessionService.iniciar(user, servletRequest, servletResponse);

        return ResponseEntity.ok(LoginResponse.fromEntity(user));

    }
}
