package com.example.meustudio.financeiro.faturamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.meustudio.shared.BusinessException;

import jakarta.persistence.criteria.Predicate;

public record FaturamentoFiltro(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        String cliente,
        String procedimento,
        BigDecimal valor,
        String meioDePagamento
) {
    public Specification<Faturamento> toSpecification() {
        if (data != null && (dataInicio != null || dataFim != null)) {
            throw new BusinessException("Informe data ou intervalo de datas, nunca ambos");
        }
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BusinessException("dataInicio deve ser anterior ou igual a dataFim");
        }
        return (root, query, cb) -> {
            var filtros = new ArrayList<Predicate>();
            if (data != null) filtros.add(cb.equal(root.get("dataFaturamento"), data));
            if (dataInicio != null) filtros.add(cb.greaterThanOrEqualTo(root.get("dataFaturamento"), dataInicio));
            if (dataFim != null) filtros.add(cb.lessThanOrEqualTo(root.get("dataFaturamento"), dataFim));
            if (cliente != null && !cliente.isBlank()) {
                filtros.add(cb.like(cb.lower(root.get("cliente")), trecho(cliente), '\\'));
            }
            if (procedimento != null && !procedimento.isBlank()) {
                filtros.add(cb.like(cb.lower(root.get("procedimento")), trecho(procedimento), '\\'));
            }
            if (valor != null) filtros.add(cb.equal(root.get("valorBrutoFaturamento"), valor));
            if (meioDePagamento != null && !meioDePagamento.isBlank()) {
                filtros.add(cb.equal(root.get("meioDePagamento"), meioDePagamento.trim()));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }

    private static String trecho(String texto) {
        return "%" + texto.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
