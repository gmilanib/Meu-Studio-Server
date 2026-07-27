package com.example.meustudio.Auth;

import com.example.meustudio.cliente.Cliente;
import com.example.meustudio.cliente.ClienteRepository;
import com.example.meustudio.cliente.ClienteResponse;
import jakarta.transaction.Transactional;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;}

    @Transactional
    public UserResponse criar (UserRequest userRequest){

        User user = new User();
        user.setUsername(userRequest.username());
        user.setPassword(userRequest.password());
        user.setEmail(userRequest.email());
//        user.setTelefone(UserRequest.telefone());
        return UserResponse.fromEntity(userRepository.save(user));
    }

}
