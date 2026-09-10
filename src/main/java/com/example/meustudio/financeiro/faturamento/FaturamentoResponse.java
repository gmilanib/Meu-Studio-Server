package com.example.meustudio.financeiro.faturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FaturamentoResponse(
        UUID id,
        LocalDate data,
        String cliente,
        String procedimento,
        BigDecimal valor,
        String meioDePagamento
) {
    public static FaturamentoResponse fromEntity(Faturamento faturamento) {
        return new FaturamentoResponse(
                faturamento.getFatID(),
                faturamento.getDataFaturamento(),
                faturamento.getCliente(),
                faturamento.getProcedimento(),
                faturamento.getValorBrutoFaturamento(),
                faturamento.getMeioDePagamento()
        );
    }
}
