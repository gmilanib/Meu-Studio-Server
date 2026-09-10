package com.example.meustudio.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ClienteRequestTest {

    @Test
    void deveAceitarEmailNulo() {
        ClienteRequest request = new ClienteRequest("Maria", null, "11999999999");

        assertNull(request.email());
    }

    @Test
    void deveConverterEmailEmBrancoParaNulo() {
        ClienteRequest request = new ClienteRequest("Maria", "   ", "11999999999");

        assertNull(request.email());
    }

    @Test
    void deveRemoverEspacosNasExtremidadesDoEmailInformado() {
        ClienteRequest request = new ClienteRequest(
                "Maria",
                "  maria@email.com  ",
                "11999999999"
        );

        assertEquals("maria@email.com", request.email());
    }
}
