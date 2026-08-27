package com.example.meustudio.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    @Test
    void deveAceitarSomenteAOrigemConfigurada() {
        String origemPermitida = "https://studio.example";
        SecurityConfig securityConfig = new SecurityConfig(origemPermitida);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/clientes");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertEquals(origemPermitida, configuration.checkOrigin(origemPermitida));
        assertNull(configuration.checkOrigin("https://attacker.example"));
        assertTrue(configuration.getAllowCredentials());
        assertEquals(
                java.util.List.of("Content-Type", "Accept", "X-XSRF-TOKEN"),
                configuration.getAllowedHeaders());
    }

    @Test
    void deveRecusarCuringaComoOrigem() {
        assertThrows(IllegalArgumentException.class, () -> new SecurityConfig("*"));
    }
}
