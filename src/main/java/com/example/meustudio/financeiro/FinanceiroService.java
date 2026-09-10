package com.example.meustudio.financeiro;

import org.springframework.stereotype.Service;

import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;

    public FinanceiroService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional
    public FaturamentoResponse criar(FaturamentoRequest request) {
        Faturamento faturamento = new Faturamento();
        faturamento.setDataFaturamento(request.data());
        faturamento.setCliente(request.cliente());
        faturamento.setProcedimento(request.procedimento());
        faturamento.setValorBrutoFaturamento(request.valor());
        faturamento.setMeioDePagamento(request.meioDePagamento());

        return FaturamentoResponse.fromEntity(financeiroRepository.save(faturamento));
    }

}
