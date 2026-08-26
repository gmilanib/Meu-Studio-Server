package com.example.meustudio.auth;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
@ConditionalOnProperty(
        name = "app.bootstrap-admin.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;
    private final String telefone;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password,
            @Value("${app.bootstrap-admin.email}") String email,
            @Value("${app.bootstrap-admin.telefone}") String telefone) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.email = email;
        this.telefone = telefone;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validarConfiguracao();

        if (userRepository.existsByRole("ADMIN")) {
            throw new IllegalStateException(
                    "Bootstrap habilitado, mas ja existe um usuario ADMIN. Desative o bootstrap.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException(
                    "Bootstrap nao pode criar o ADMIN porque o username informado ja existe.");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setTelefone(telefone);
        admin.setRole("ADMIN");

        userRepository.save(admin);
        LOGGER.info("Bootstrap do primeiro usuario ADMIN concluido.");
    }

    private void validarConfiguracao() {
        boolean configuracaoIncompleta = Stream.of(username, password, email, telefone)
                .anyMatch(valor -> valor == null || valor.isBlank());

        if (configuracaoIncompleta) {
            throw new IllegalStateException(
                    "Bootstrap do ADMIN habilitado com configuracao incompleta.");
        }
    }
}
