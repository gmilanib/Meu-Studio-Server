package com.example.meustudio.auth;

import com.example.meustudio.config.Encoder;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Encoder encoder;

    public UserService(UserRepository userRepository, Encoder encoder){
        this.userRepository = userRepository;
    this.encoder = encoder;}

    @Transactional
    public UserResponse criar (UserRequest userRequest){

        String senhaHash = encoder.passwordEncoder().encode(userRequest.password());

        User user = new User();
        user.setUsername(userRequest.username());
        user.setPassword(senhaHash);
        user.setEmail(userRequest.email());
        user.setTelefone(userRequest.telefone());


        return UserResponse.fromEntity(userRepository.save(user));
    }

}
