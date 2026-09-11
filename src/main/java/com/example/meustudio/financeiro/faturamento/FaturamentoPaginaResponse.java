package com.example.meustudio.financeiro.faturamento;

import java.util.List;
import org.springframework.data.domain.Page;

public record FaturamentoPaginaResponse(
        List<FaturamentoResponse> content, int page, int size, long totalElements, int totalPages
) {
    public static FaturamentoPaginaResponse fromPage(Page<FaturamentoResponse> pagina) {
        return new FaturamentoPaginaResponse(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }
}
