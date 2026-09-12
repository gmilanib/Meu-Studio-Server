package com.example.meustudio.procedimento;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcedimentoResponse(UUID id, String nome, String descricao, BigDecimal preco,
        Integer duracaoMinutos, String categoria, boolean ativo) {
    public static ProcedimentoResponse fromEntity(Procedimento procedimento) {
        return new ProcedimentoResponse(procedimento.getId(), procedimento.getNome(), procedimento.getDescricao(),
                procedimento.getPreco(), procedimento.getDuracaoMinutos(), procedimento.getCategoria(), procedimento.isAtivo());
    }
}
