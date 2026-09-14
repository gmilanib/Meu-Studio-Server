package com.example.meustudio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
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
import com.example.meustudio.shared.BusinessException;

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

    @Test
    void assumeHorarioAtualDeSaoPauloQuandoNaoInformado() {
        var faturamentos = mock(FinanceiroRepository.class);
        var procedimentos = mock(ProcedimentoService.class);
        var procedimento = procedimento();
        when(procedimentos.exigirAtivo(procedimento.getId())).thenReturn(procedimento);
        when(faturamentos.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        var clock = Clock.fixed(Instant.parse("2026-09-14T21:37:42Z"), ZoneId.of("UTC"));
        var service = new FinanceiroService(faturamentos, procedimentos, mock(ClienteRepository.class), clock);

        var resposta = service.criar(new FaturamentoRequest(LocalDate.of(2026, 9, 14), null,
                "Maria", null, procedimento.getId(), new BigDecimal("150.00"), "PIX"));

        assertEquals(LocalTime.of(18, 37), resposta.horario());
    }

    @Test
    void rejeitaDataEHorarioFuturos() {
        var clock = Clock.fixed(Instant.parse("2026-09-14T21:00:00Z"), ZoneId.of("UTC"));
        var service = new FinanceiroService(mock(FinanceiroRepository.class), mock(ProcedimentoService.class),
                mock(ClienteRepository.class), clock);
        var futuroNoMesmoDia = new FaturamentoRequest(LocalDate.of(2026, 9, 14), LocalTime.of(18, 1),
                "Maria", null, UUID.randomUUID(), new BigDecimal("150.00"), "PIX");

        var erro = assertThrows(BusinessException.class, () -> service.criar(futuroNoMesmoDia));

        assertEquals("A data e o horário do faturamento não podem estar no futuro", erro.getMessage());
    }

    private Procedimento procedimento() {
        var procedimento = new Procedimento();
        procedimento.atualizar(new ProcedimentoRequest("Design", null, new BigDecimal("150.00"), 30, null));
        return procedimento;
    }

    private FaturamentoRequest request(String cliente, Long clienteId, UUID procedimentoId) {
        return new FaturamentoRequest(LocalDate.of(2026, 9, 10), LocalTime.of(18, 30), cliente, clienteId, procedimentoId,
                new BigDecimal("150.00"), "PIX");
    }
}
