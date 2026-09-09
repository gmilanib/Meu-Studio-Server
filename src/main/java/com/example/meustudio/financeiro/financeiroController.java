package com.example.meustudio.financeiro;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

@RestController
@RequestMapping("/financeiro")
public class financeiroController {

    private final FinanceiroService financeiroService;

    public financeiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @PostMapping("/lancar")
    public ResponseEntity<FaturamentoResponse> lancarFaturamento(@RequestBody FaturamentoRequest request) {
        FaturamentoResponse response = faturamento.lancar(request);
        return faturamentoResponse;
    }

}
