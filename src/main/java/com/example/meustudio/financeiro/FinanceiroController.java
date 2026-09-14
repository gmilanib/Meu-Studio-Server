package com.example.meustudio.financeiro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.meustudio.financeiro.faturamento.FaturamentoFiltro;
import com.example.meustudio.financeiro.faturamento.FaturamentoPaginaResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.validation.Valid;
import java.util.UUID;

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

    @GetMapping("/faturamentos")
    public FaturamentoPaginaResponse listarFaturamentos(
            @ModelAttribute FaturamentoFiltro filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return financeiroService.listar(filtro, page, size);
    }

    @PutMapping("/faturamentos/{id}")
    public FaturamentoResponse atualizarFaturamento(@PathVariable UUID id,
            @Valid @RequestBody FaturamentoRequest request) {
        return financeiroService.atualizar(id, request);
    }

    @DeleteMapping("/faturamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirFaturamento(@PathVariable UUID id) {
        financeiroService.excluir(id);
    }

}
