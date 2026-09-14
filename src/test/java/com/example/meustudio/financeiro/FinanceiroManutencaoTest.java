package com.example.meustudio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.meustudio.cliente.Cliente;
import com.example.meustudio.cliente.ClienteRepository;
import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.procedimento.Procedimento;
import com.example.meustudio.procedimento.ProcedimentoRequest;
import com.example.meustudio.procedimento.ProcedimentoService;
import com.example.meustudio.shared.NotFoundException;

class FinanceiroManutencaoTest {

    @Test
    void vinculaClienteSelecionadoEExibeSeuNomeAtual() {
        var faturamentos = mock(FinanceiroRepository.class);
        var clientes = mock(ClienteRepository.class);
        var procedimentos = mock(ProcedimentoService.class);
        var cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(7L);
        when(cliente.getNome()).thenReturn("Maria Atualizada");
        when(clientes.findById(7L)).thenReturn(Optional.of(cliente));
        var procedimento = procedimento();
        when(procedimentos.exigirAtivo(procedimento.getId())).thenReturn(procedimento);
        when(faturamentos.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = new FinanceiroService(faturamentos, procedimentos, clientes)
                .criar(request("Nome ignorado", 7L, procedimento.getId()));

        assertEquals("Maria Atualizada", resposta.cliente());
        assertEquals(7L, resposta.clienteId());
    }

    @Test
    void preservaTextoLivreSemCriarVinculo() {
        var faturamentos = mock(FinanceiroRepository.class);
        var procedimentos = mock(ProcedimentoService.class);
        var procedimento = procedimento();
        when(procedimentos.exigirAtivo(procedimento.getId())).thenReturn(procedimento);
        when(faturamentos.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = new FinanceiroService(faturamentos, procedimentos, mock(ClienteRepository.class))
                .criar(request("  Cliente avulso  ", null, procedimento.getId()));

        assertEquals("Cliente avulso", resposta.cliente());
        assertNull(resposta.clienteId());
    }

    @Test
    void associaAutomaticamenteQuandoExisteUmUnicoNomeEquivalente() {
        var faturamentos = mock(FinanceiroRepository.class);
        var clientes = mock(ClienteRepository.class);
        var procedimentos = mock(ProcedimentoService.class);
        var cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(8L);
        when(cliente.getNome()).thenReturn("Maria");
        when(clientes.findByNomeNormalizado("  MARIA  ")).thenReturn(List.of(cliente));
        var procedimento = procedimento();
        when(procedimentos.exigirAtivo(procedimento.getId())).thenReturn(procedimento);
        when(faturamentos.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = new FinanceiroService(faturamentos, procedimentos, clientes)
                .criar(request("  MARIA  ", null, procedimento.getId()));

        assertEquals(8L, resposta.clienteId());
        assertEquals("Maria", resposta.cliente());
    }

    @Test
    void editaEExcluiLancamentoERejeitaIdsInexistentes() {
        var faturamentos = mock(FinanceiroRepository.class);
        var procedimentos = mock(ProcedimentoService.class);
        var clientes = mock(ClienteRepository.class);
        var procedimento = procedimento();
        var id = UUID.randomUUID();
        var existente = new Faturamento();
        when(faturamentos.findById(id)).thenReturn(Optional.of(existente));
        when(procedimentos.exigirAtivo(procedimento.getId())).thenReturn(procedimento);
        when(faturamentos.save(existente)).thenReturn(existente);
        when(faturamentos.existsById(id)).thenReturn(true);
        var service = new FinanceiroService(faturamentos, procedimentos, clientes);

        var resposta = service.atualizar(id, request("Cliente livre", null, procedimento.getId()));
        service.excluir(id);

        assertEquals("Cliente livre", resposta.cliente());
        verify(faturamentos).deleteById(id);
        var ausente = UUID.randomUUID();
        assertThrows(NotFoundException.class,
                () -> service.atualizar(ausente, request("Cliente", null, procedimento.getId())));
        assertThrows(NotFoundException.class, () -> service.excluir(ausente));
    }

    private Procedimento procedimento() {
        var procedimento = new Procedimento();
        procedimento.atualizar(new ProcedimentoRequest("Design", null, new BigDecimal("150.00"), 30, null));
        return procedimento;
    }

    private FaturamentoRequest request(String cliente, Long clienteId, UUID procedimentoId) {
        return new FaturamentoRequest(LocalDate.of(2026, 9, 10), cliente, clienteId, procedimentoId,
                new BigDecimal("150.00"), "PIX");
    }
}
