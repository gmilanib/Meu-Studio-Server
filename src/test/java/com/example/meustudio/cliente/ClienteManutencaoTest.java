package com.example.meustudio.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.meustudio.financeiro.FinanceiroRepository;
import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.shared.NotFoundException;

class ClienteManutencaoTest {

    @Test
    void atualizaCamposPermitidosEPreservaEmailOcultoEnviadoPeloFrontend() {
        var clientes = mock(ClienteRepository.class);
        var financeiro = mock(FinanceiroRepository.class);
        var cliente = new Cliente();
        cliente.setNome("Maria");
        cliente.setEmail("maria@studio.com");
        cliente.setTelefone("1111");
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(clientes.save(cliente)).thenReturn(cliente);

        var resposta = new ClienteService(clientes, financeiro)
                .atualizar(1L, new ClienteRequest("Maria Silva", "maria@studio.com", "2222"));

        assertEquals("Maria Silva", resposta.nome());
        assertEquals("maria@studio.com", resposta.email());
        assertEquals("2222", resposta.telefone());
    }

    @Test
    void bloqueiaExclusaoQuandoClientePossuiLancamentos() {
        var clientes = mock(ClienteRepository.class);
        var financeiro = mock(FinanceiroRepository.class);
        when(clientes.existsById(1L)).thenReturn(true);
        when(financeiro.existsByClienteCadastradoId(1L)).thenReturn(true);

        var erro = assertThrows(BusinessException.class,
                () -> new ClienteService(clientes, financeiro).excluir(1L));

        assertEquals("Cliente possui lançamentos financeiros. Altere ou exclua esses lançamentos na aba Financeiro antes de excluir o cliente.", erro.getMessage());
        verify(clientes, never()).deleteById(1L);
    }

    @Test
    void excluiClienteSemLancamentosERejeitaIdInexistente() {
        var clientes = mock(ClienteRepository.class);
        var financeiro = mock(FinanceiroRepository.class);
        when(clientes.existsById(1L)).thenReturn(true);
        var service = new ClienteService(clientes, financeiro);

        service.excluir(1L);

        verify(clientes).deleteById(1L);
        assertThrows(NotFoundException.class, () -> service.excluir(2L));
    }
}
