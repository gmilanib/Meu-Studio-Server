package com.example.meustudio.auth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

class SessionServiceTest {

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveTrocarOIdentificadorDaSessaoDepoisDoLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String identificadorAntesDoLogin = request.getSession().getId();

        SessionService sessionService = new SessionService(
                new HttpSessionSecurityContextRepository(),
                new ChangeSessionIdAuthenticationStrategy());

        User user = new User();
        user.setUsername("admin-teste");
        user.setRole("ADMIN");

        sessionService.iniciar(user, request, response);

        assertNotNull(request.getSession(false));
        assertNotEquals(
                identificadorAntesDoLogin,
                request.getSession(false).getId(),
                "O login deve trocar o ID da sessao para impedir fixacao de sessao");
    }
}
