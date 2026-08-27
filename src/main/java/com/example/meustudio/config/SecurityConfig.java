package com.example.meustudio.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final String allowedOrigin;

        public SecurityConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
                if (allowedOrigin.isBlank() || "*".equals(allowedOrigin)) {
                        throw new IllegalArgumentException(
                                        "A origem CORS deve ser explicita e nao pode usar curinga");
                }
                this.allowedOrigin = allowedOrigin;
        }

        @Bean
        public SecurityContextRepository securityContextRepository() {
                return new HttpSessionSecurityContextRepository();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        SecurityContextRepository securityContextRepository) {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .securityContext(securityContext -> securityContext
                                                .securityContextRepository(securityContextRepository))

                                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/csrf").permitAll()
                                                .requestMatchers("/auth/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/auth/CreateUser")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/clientes/**")
                                                .hasAnyRole("ADMIN", "USER")
                                                .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                List.of(allowedOrigin));

                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("Content-Type", "Accept", "X-XSRF-TOKEN"));

                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        @Bean
        public SessionAuthenticationStrategy sessionAtuhenticationStrategy() {
                return new ChangeSessionIdAuthenticationStrategy();
        }
}
