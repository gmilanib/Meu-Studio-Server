package com.example.meustudio.financeiro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @PostMapping("/lancar")
    public ResponseEntity<FaturamentoResponse> lancarFaturamento(
            @Valid @RequestBody FaturamentoRequest request) {
        FaturamentoResponse response = financeiroService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
