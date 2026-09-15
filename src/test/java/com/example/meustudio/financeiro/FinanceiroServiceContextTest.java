package com.example.meustudio.financeiro;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.meustudio.cliente.ClienteRepository;
import com.example.meustudio.procedimento.ProcedimentoService;

class FinanceiroServiceContextTest {

    @Test
    void deveCriarServicoComDependenciasSemExigirBeanClock() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(FinanceiroRepository.class, () -> mock(FinanceiroRepository.class));
            context.registerBean(ProcedimentoService.class, () -> mock(ProcedimentoService.class));
            context.registerBean(ClienteRepository.class, () -> mock(ClienteRepository.class));
            context.register(FinanceiroService.class);
            context.refresh();

            assertNotNull(context.getBean(FinanceiroService.class));
        }
    }
}
