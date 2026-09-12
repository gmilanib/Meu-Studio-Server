package com.example.meustudio.procedimento;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProcedimentoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 160, message = "Nome deve ter no máximo 160 caracteres") String nome,
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres") String descricao,
        @NotNull(message = "Preço é obrigatório")
        @Positive(message = "Preço deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Preço deve ter até 10 inteiros e 2 casas decimais") BigDecimal preco,
        @NotNull(message = "Duração é obrigatória")
        @Positive(message = "Duração deve ser um número inteiro positivo de minutos") Integer duracaoMinutos,
        @Size(max = 80, message = "Categoria deve ter no máximo 80 caracteres") String categoria
) {}
