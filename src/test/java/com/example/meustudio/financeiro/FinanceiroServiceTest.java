package com.example.meustudio.financeiro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

class FinanceiroServiceTest {

    @Test
    void deveCriarLancamentoDeFaturamento() {
        UUID id = UUID.randomUUID();
        AtomicReference<Faturamento> faturamentoSalvo = new AtomicReference<>();
        FinanceiroRepository repository = criarRepository(id, faturamentoSalvo);
        FinanceiroService financeiroService = new FinanceiroService(repository);
        LocalDate data = LocalDate.of(2026, 9, 10);
        BigDecimal valor = new BigDecimal("150.00");
        FaturamentoRequest request = new FaturamentoRequest(
                data,
                "Maria da Silva",
                "Design de sobrancelhas",
                valor,
                "PIX"
        );

        FaturamentoResponse response = financeiroService.criar(request);

        Faturamento salvo = faturamentoSalvo.get();
        assertEquals(data, salvo.getDataFaturamento());
        assertEquals("Maria da Silva", salvo.getCliente());
        assertEquals("Design de sobrancelhas", salvo.getProcedimento());
        assertEquals(valor, salvo.getValorBrutoFaturamento());
        assertEquals("PIX", salvo.getMeioDePagamento());
        assertEquals(id, response.id());
        assertEquals(data, response.data());
        assertEquals("Maria da Silva", response.cliente());
        assertEquals("Design de sobrancelhas", response.procedimento());
        assertEquals(valor, response.valor());
        assertEquals("PIX", response.meioDePagamento());
    }

    private FinanceiroRepository criarRepository(
            UUID id,
            AtomicReference<Faturamento> faturamentoSalvo) {
        return (FinanceiroRepository) Proxy.newProxyInstance(
                FinanceiroRepository.class.getClassLoader(),
                new Class<?>[]{FinanceiroRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        Faturamento faturamento = (Faturamento) args[0];
                        faturamento.setFatID(id);
                        faturamentoSalvo.set(faturamento);
                        return faturamento;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
