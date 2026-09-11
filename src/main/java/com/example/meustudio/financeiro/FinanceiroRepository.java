package com.example.meustudio.financeiro;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.meustudio.financeiro.faturamento.Faturamento;

public interface FinanceiroRepository extends JpaRepository<Faturamento, UUID>, JpaSpecificationExecutor<Faturamento> {
}
