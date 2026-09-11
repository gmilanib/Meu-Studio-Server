package com.example.meustudio.cliente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.meustudio.shared.BusinessException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

class ClienteConsultaTest {
    private final LocalDate dia = LocalDate.of(2026, 9, 11);

    @Test
    void rejeitaConflitosEIntervalosInvertidosNosDoisCampos() {
        assertThrows(BusinessException.class, () -> filtro(dia, dia, null, null, null, null).toSpecification());
        assertThrows(BusinessException.class, () -> filtro(null, dia, dia.minusDays(1), null, null, null).toSpecification());
        assertThrows(BusinessException.class, () -> filtro(null, null, null, dia, null, dia).toSpecification());
        assertThrows(BusinessException.class, () -> filtro(null, null, null, null, dia, dia.minusDays(1)).toSpecification());
        assertDoesNotThrow(() -> filtro(null, dia, dia, null, null, dia).toSpecification());
    }

    @Test
    void rejeitaPaginacaoInvalidaSemAcessarBanco() {
        var repository = mock(ClienteRepository.class);
        var service = new ClienteService(repository);
        var filtro = filtro(null, null, null, null, null, null);
        assertThrows(BusinessException.class, () -> service.listar(filtro, -1, 50));
        assertThrows(BusinessException.class, () -> service.listar(filtro, 0, 0));
        assertThrows(BusinessException.class, () -> service.listar(filtro, 0, 51));
        verifyNoInteractions(repository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void paginaNoBancoComOrdenacaoEstavelEMetadados() {
        var repository = mock(ClienteRepository.class);
        var cliente = new Cliente();
        cliente.setNome("Maria");
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(1);
            assertEquals(1, pageable.getPageNumber());
            assertEquals(50, pageable.getPageSize());
            assertTrue(pageable.getSort().getOrderFor("criadoEm").isDescending());
            assertTrue(pageable.getSort().getOrderFor("id").isDescending());
            return new PageImpl<>(List.of(cliente), pageable, 51);
        });
        var pagina = new ClienteService(repository).listar(filtro(null, null, null, null, null, null), 1, 50);
        assertEquals("Maria", pagina.content().getFirst().nome());
        assertEquals(51, pagina.totalElements());
        assertEquals(2, pagina.totalPages());
    }

    @Test
    @SuppressWarnings("unchecked")
    void combinaTextosLiteraisEDiasComLimiteSuperiorExclusivo() {
        Root<Cliente> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<LocalDateTime> criado = mock(Path.class);
        Path<LocalDateTime> atualizado = mock(Path.class);
        Expression<String> minusculo = mock(Expression.class);
        when(root.<LocalDateTime>get("criadoEm")).thenReturn(criado);
        when(root.<LocalDateTime>get("atualizadoEm")).thenReturn(atualizado);
        when(cb.lower(any())).thenReturn(minusculo);
        var filtro = new ClienteFiltro(" MAR%_ ", "MAIL", "123", dia, null, null,
                null, dia.minusDays(1), dia);
        filtro.toSpecification().toPredicate(root, null, cb);
        verify(cb).like(minusculo, "%mar\\%\\_%", '\\');
        verify(cb).like(minusculo, "%mail%", '\\');
        verify(cb).like(minusculo, "%123%", '\\');
        verify(cb).greaterThanOrEqualTo(criado, dia.atStartOfDay());
        verify(cb).lessThan(criado, dia.plusDays(1).atStartOfDay());
        verify(cb).greaterThanOrEqualTo(atualizado, dia.minusDays(1).atStartOfDay());
        verify(cb).lessThan(atualizado, dia.plusDays(1).atStartOfDay());
        var predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(predicates.capture());
        assertEquals(7, predicates.getValue().length);
    }

    @Test
    @SuppressWarnings("unchecked")
    void permiteConsultaSemFiltrosEIgnoraTextosEmBranco() {
        Root<Cliente> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        new ClienteFiltro(" ", "", null, null, null, null, null, null, null)
                .toSpecification().toPredicate(root, null, cb);
        verify(cb).and(new Predicate[0]);
        verifyNoMoreInteractions(cb);
    }

    private ClienteFiltro filtro(LocalDate criado, LocalDate criadoInicio, LocalDate criadoFim,
            LocalDate atualizado, LocalDate atualizadoInicio, LocalDate atualizadoFim) {
        return new ClienteFiltro(null, null, null, criado, criadoInicio, criadoFim,
                atualizado, atualizadoInicio, atualizadoFim);
    }
}
