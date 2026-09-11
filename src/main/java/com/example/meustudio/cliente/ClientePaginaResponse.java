package com.example.meustudio.cliente;

import java.util.List;
import org.springframework.data.domain.Page;

public record ClientePaginaResponse(
        List<ClienteResponse> content, int page, int size, long totalElements, int totalPages
) {
    public static ClientePaginaResponse fromPage(Page<ClienteResponse> pagina) {
        return new ClientePaginaResponse(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
