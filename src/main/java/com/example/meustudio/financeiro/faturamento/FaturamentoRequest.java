package com.example.meustudio.financeiro.faturamento;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record FaturamentoRequest(
    @NotBlank (message = "O campo data é obrigatório")
    LocalDate data,
    @NotBlank (message = "O campo cliente é obrigatório")
    String cliente,
    @NotBlank (message = "O campo procedimento é obrigatório")
    String procedimento,
    @NotBlank (message = "O campo valor é obrigatório")
    Float valor,
    @NotBlank (message = "O campo meio de pagamento é obrigatório")
    String meioDePagamento
) {
}
