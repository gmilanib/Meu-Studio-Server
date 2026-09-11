package com.example.meustudio.financeiro;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoFiltro;
import com.example.meustudio.shared.BusinessException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

class FaturamentoConsultaTest {
    private final LocalDate inicio = LocalDate.of(2026, 9, 1);
    private final LocalDate fim = LocalDate.of(2026, 9, 10);

    @Test
    void rejeitaDatasConflitantesEIntervaloInvertido() {
        assertThrows(BusinessException.class, () -> filtro(inicio, inicio, fim).toSpecification());
        assertThrows(BusinessException.class, () -> filtro(null, fim, inicio).toSpecification());
        assertDoesNotThrow(() -> filtro(null, inicio, inicio).toSpecification());
        assertDoesNotThrow(() -> filtro(null, inicio, null).toSpecification());
        assertDoesNotThrow(() -> filtro(null, null, fim).toSpecification());
    }

    @Test
    void rejeitaPaginacaoForaDosLimitesAntesDeConsultarBanco() {
        var repository = mock(FinanceiroRepository.class);
        var service = new FinanceiroService(repository);
        var filtro = filtro(null, null, null);
        assertThrows(BusinessException.class, () -> service.listar(filtro, -1, 50));
        assertThrows(BusinessException.class, () -> service.listar(filtro, 0, 0));
        assertThrows(BusinessException.class, () -> service.listar(filtro, 0, 51));
        verifyNoInteractions(repository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void paginaNoBancoEConservaMetadados() {
        var repository = mock(FinanceiroRepository.class);
        var faturamento = new Faturamento();
        faturamento.setCliente("Maria");
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            assertEquals(1, pageable.getPageNumber());
            assertEquals(50, pageable.getPageSize());
            assertEquals("dataFaturamento: DESC,fatID: DESC", pageable.getSort().toString().replace(", ", ","));
            return new PageImpl<>(List.of(faturamento), pageable, 51);
        });
        var resultado = new FinanceiroService(repository).listar(filtro(null, null, null), 1, 50);
        assertEquals(51, resultado.totalElements());
        assertEquals(2, resultado.totalPages());
        assertEquals("Maria", resultado.content().getFirst().cliente());
    }

    @Test
    @SuppressWarnings("unchecked")
    void usaTrechoLiteralMinusculoELimitesInclusivos() {
        Root<Faturamento> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Expression<String> minusculo = mock(Expression.class);
        when(cb.lower(any())).thenReturn(minusculo);
        var filtro = new FaturamentoFiltro(null, inicio, fim, "MAR%_", "LIMPEZA", null, null);
        filtro.toSpecification().toPredicate(root, null, cb);
        verify(cb).like(minusculo, "%mar\\%\\_%", '\\');
        verify(cb).like(minusculo, "%limpeza%", '\\');
        verify(cb).greaterThanOrEqualTo(root.<LocalDate>get("dataFaturamento"), inicio);
        verify(cb).lessThanOrEqualTo(root.<LocalDate>get("dataFaturamento"), fim);
    }

    private FaturamentoFiltro filtro(LocalDate data, LocalDate de, LocalDate ate) {
        return new FaturamentoFiltro(data, de, ate, null, null, null, null);
    }
}
