package com.example.meustudio.financeiro.faturamento;

import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID fatID;

    @Column(nullable = false, name = "datafaturamento")
    private LocalDateTime dataFaturamento;

    @Column(nullable = false, name = "clienteid")
    private long clienteID;

    @Column(nullable = false, name = "valorbrutofaturamento")
    private float valorBrutoFaturamento;

    public Faturamento() {
    }

    public Faturamento(UUID fatID, LocalDateTime dataFaturamento, long clienteID, float valorBrutoFaturamento) {
        this.fatID = fatID;
        this.dataFaturamento = dataFaturamento;
        this.clienteID = clienteID;
        this.valorBrutoFaturamento = valorBrutoFaturamento;
    }

    public UUID getFatID() {
        return fatID;
    }

    public void setFatID(UUID fatID) {
        this.fatID = fatID;
    }

    public LocalDateTime getDataFaturamento() {
        return dataFaturamento;
    }

    public void setDataFaturamento(LocalDateTime dataFaturamento) {
        this.dataFaturamento = dataFaturamento;
    }

    public long getClienteID() {
        return clienteID;
    }

    public void setClienteID(long clienteID) {
        this.clienteID = clienteID;
    }

    public float getValorBrutoFaturamento() {
        return valorBrutoFaturamento;
    }

    public void setValorBrutoFaturamento(float valorBrutoFaturamento) {
        this.valorBrutoFaturamento = valorBrutoFaturamento;
    }

}
