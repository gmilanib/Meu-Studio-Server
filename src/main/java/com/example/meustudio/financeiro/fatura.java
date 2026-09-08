package com.example.meustudio.financeiro;

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
public class fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID fatID;

    @Column(nullable = false, name = "datafaturamento")
    private LocalDateTime dataFaturamento;

    @Column(nullable = false, name = "clienteid")
    private long clienteID;

    @Column(nullable = false, name = "valorbrutofaturamento")
    private float valorBrutoFaturamento;

}
