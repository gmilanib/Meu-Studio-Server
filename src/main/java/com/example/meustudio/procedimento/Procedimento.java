package com.example.meustudio.procedimento;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "procedimentos")
public class Procedimento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 160)
    private String nome;
    @Column(length = 2000)
    private String descricao;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;
    @Column(length = 80)
    private String categoria;
    @Column(nullable = false)
    private boolean ativo = true;

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public String getCategoria() { return categoria; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public void atualizar(ProcedimentoRequest request) {
        nome = request.nome().trim();
        descricao = textoOpcional(request.descricao());
        preco = request.preco();
        duracaoMinutos = request.duracaoMinutos();
        categoria = textoOpcional(request.categoria());
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
