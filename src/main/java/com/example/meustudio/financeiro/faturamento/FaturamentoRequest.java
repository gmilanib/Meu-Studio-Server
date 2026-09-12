package com.example.meustudio.financeiro.faturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FaturamentoRequest(
        @NotNull(message = "O campo data é obrigatório")
        @PastOrPresent(message = "A data do faturamento não pode estar no futuro")
        LocalDate data,

        @NotBlank(message = "O campo cliente é obrigatório")
        @Size(max = 120, message = "Cliente deve ter no máximo 120 caracteres")
        String cliente,

        @NotNull(message = "Selecione um procedimento ativo")
        UUID procedimentoId,

        @NotNull(message = "O campo valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 10 inteiros e 2 casas decimais")
        BigDecimal valor,

        @NotBlank(message = "O campo meio de pagamento é obrigatório")
        @Size(max = 30, message = "Meio de pagamento deve ter no máximo 30 caracteres")
        String meioDePagamento
) {
}
