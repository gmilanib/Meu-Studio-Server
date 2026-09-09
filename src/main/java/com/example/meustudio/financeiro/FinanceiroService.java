package com.example.meustudio.financeiro;

import org.springframework.stereotype.Service;

import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;

    public FinanceiroService(FinanceiroRepository financeiroRepository){
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional
    public FaturamentoResponse criar (FaturamentoRequest request){
        Faturamento faturamento = new Faturmaneto(request.data(), request.cliente(), request.valor(), request.meioDePagamento());



    }

}
