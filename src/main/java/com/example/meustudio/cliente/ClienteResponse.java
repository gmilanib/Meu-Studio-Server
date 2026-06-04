package com.example.meustudio.cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getCriadoEm(),
                cliente.getAtualizadoEm()
        );
    }
}
