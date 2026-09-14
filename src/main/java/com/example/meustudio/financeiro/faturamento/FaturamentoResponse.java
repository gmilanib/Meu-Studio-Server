package com.example.meustudio.financeiro.faturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

public record FaturamentoResponse(
        UUID id,
        LocalDate data,
        @JsonFormat(pattern = "HH:mm") LocalTime horario,
        String cliente,
        Long clienteId,
        String procedimento,
        UUID procedimentoId,
        BigDecimal valor,
        String meioDePagamento
) {
    public static FaturamentoResponse fromEntity(Faturamento faturamento) {
        return new FaturamentoResponse(
                faturamento.getFatID(),
                faturamento.getDataFaturamento(),
                faturamento.getHorarioFaturamento(),
                faturamento.getClienteCadastrado() == null
                        ? faturamento.getCliente()
                        : faturamento.getClienteCadastrado().getNome(),
                faturamento.getClienteCadastrado() == null
                        ? null
                        : faturamento.getClienteCadastrado().getId(),
                faturamento.getProcedimento(),
                faturamento.getProcedimentoId(),
                faturamento.getValorBrutoFaturamento(),
                faturamento.getMeioDePagamento()
        );
    }
}
