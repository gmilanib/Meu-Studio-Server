package com.example.meustudio.cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.meustudio.shared.BusinessException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public record ClienteFiltro(
        String nome,
        String email,
        String telefone,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoEm,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoEmInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate criadoEmFim,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoEm,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoEmInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate atualizadoEmFim
) {
    public Specification<Cliente> toSpecification() {
        validarDatas("criadoEm", criadoEm, criadoEmInicio, criadoEmFim);
        validarDatas("atualizadoEm", atualizadoEm, atualizadoEmInicio, atualizadoEmFim);
        return (root, query, cb) -> {
            var filtros = new ArrayList<Predicate>();
            adicionarTexto(filtros, cb, root.get("nome"), nome);
            adicionarTexto(filtros, cb, root.get("email"), email);
            adicionarTexto(filtros, cb, root.get("telefone"), telefone);
            adicionarDatas(filtros, cb, root.get("criadoEm"), criadoEm, criadoEmInicio, criadoEmFim);
            adicionarDatas(filtros, cb, root.get("atualizadoEm"), atualizadoEm, atualizadoEmInicio, atualizadoEmFim);
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }

    private static void validarDatas(String campo, LocalDate dia, LocalDate inicio, LocalDate fim) {
        if (dia != null && (inicio != null || fim != null)) {
            throw new BusinessException("Informe " + campo + " ou seu intervalo, nunca ambos");
        }
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new BusinessException(campo + "Inicio deve ser anterior ou igual a " + campo + "Fim");
        }
        if (LocalDate.MAX.equals(dia) || LocalDate.MAX.equals(fim)) {
            throw new BusinessException("Data fora do limite permitido para " + campo);
        }
    }

    private static void adicionarTexto(List<Predicate> filtros, CriteriaBuilder cb, Path<String> campo, String texto) {
        if (texto != null && !texto.isBlank()) {
            String trecho = texto.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            filtros.add(cb.like(cb.lower(campo), "%" + trecho + "%", '\\'));
        }
    }

    private static void adicionarDatas(List<Predicate> filtros, CriteriaBuilder cb, Path<LocalDateTime> campo,
            LocalDate dia, LocalDate inicio, LocalDate fim) {
        LocalDate de = dia != null ? dia : inicio;
        LocalDate ate = dia != null ? dia : fim;
        if (de != null) filtros.add(cb.greaterThanOrEqualTo(campo, de.atStartOfDay()));
        if (ate != null) filtros.add(cb.lessThan(campo, ate.plusDays(1).atStartOfDay()));
    }
}
