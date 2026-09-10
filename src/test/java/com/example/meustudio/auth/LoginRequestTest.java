package com.example.meustudio.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.example.meustudio.auth.Login.LoginRequest;

class LoginRequestTest {

    @Test
    void deveRemoverEspacosEmBrancoNasExtremidadesDoUsername() {
        LoginRequest request = new LoginRequest("  usuario\t", "senha");

        assertEquals("usuario", request.username());
    }

    @Test
    void naoDeveAlterarEspacosDaSenha() {
        LoginRequest request = new LoginRequest("usuario", " senha ");

        assertEquals(" senha ", request.password());
    }

    @Test
    void deveAceitarUsernameNuloParaQueAValidacaoRetorneErroDeCampo() {
        LoginRequest request = new LoginRequest(null, "senha");

        assertNull(request.username());
    }
}
