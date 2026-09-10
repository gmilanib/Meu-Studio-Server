package com.example.meustudio.financeiro.faturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "faturamentos")
public class Faturamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fatid", nullable = false, updatable = false)
    private UUID fatID;

    @Column(nullable = false, name = "datafaturamento")
    private LocalDate dataFaturamento;

    @Column(nullable = false, name = "clientename", length = 120)
    private String cliente;

    @Column(nullable = false, length = 160)
    private String procedimento;

    @Column(nullable = false, name = "valorbrutofaturamento", precision = 12, scale = 2)
    private BigDecimal valorBrutoFaturamento;

    @Column(nullable = false, name = "meiopagamento", length = 30)
    private String meioDePagamento;

    public Faturamento() {
    }

    public UUID getFatID() {
        return fatID;
    }

    public void setFatID(UUID fatID) {
        this.fatID = fatID;
    }

    public LocalDate getDataFaturamento() {
        return dataFaturamento;
    }

    public void setDataFaturamento(LocalDate dataFaturamento) {
        this.dataFaturamento = dataFaturamento;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getProcedimento() {
        return procedimento;
    }

    public void setProcedimento(String procedimento) {
        this.procedimento = procedimento;
    }

    public BigDecimal getValorBrutoFaturamento() {
        return valorBrutoFaturamento;
    }

    public void setValorBrutoFaturamento(BigDecimal valorBrutoFaturamento) {
        this.valorBrutoFaturamento = valorBrutoFaturamento;
    }

    public String getMeioDePagamento() {
        return meioDePagamento;
    }

    public void setMeioDePagamento(String meioDePagamento) {
        this.meioDePagamento = meioDePagamento;
    }

}
