package com.example.meustudio.Auth;

import com.example.meustudio.cliente.ClienteRequest;
import com.example.meustudio.cliente.ClienteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/auth")
public class UserController {
    private final UserService userService;

    public UserController (UserService userService){this.userService = userService;}

    @CrossOrigin("*")
    @PostMapping("/CreateUser")
    public ResponseEntity<UserResponse> criar(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
