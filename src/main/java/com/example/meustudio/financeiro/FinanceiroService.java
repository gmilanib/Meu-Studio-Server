package com.example.meustudio.financeiro;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.meustudio.financeiro.faturamento.FaturamentoFiltro;
import com.example.meustudio.financeiro.faturamento.FaturamentoPaginaResponse;
import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.procedimento.ProcedimentoService;

import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;
    private final ProcedimentoService procedimentoService;

    public FinanceiroService(FinanceiroRepository financeiroRepository, ProcedimentoService procedimentoService) {
        this.financeiroRepository = financeiroRepository;
        this.procedimentoService = procedimentoService;
    }

    @Transactional
    public FaturamentoResponse criar(FaturamentoRequest request) {
        var procedimento = procedimentoService.exigirAtivo(request.procedimentoId());
        Faturamento faturamento = new Faturamento();
        faturamento.setDataFaturamento(request.data());
        faturamento.setCliente(request.cliente());
        faturamento.setProcedimento(procedimento.getNome());
        faturamento.setProcedimentoId(procedimento.getId());
        faturamento.setValorBrutoFaturamento(request.valor());
        faturamento.setMeioDePagamento(request.meioDePagamento());

        return FaturamentoResponse.fromEntity(financeiroRepository.save(faturamento));
    }

    public FaturamentoPaginaResponse listar(FaturamentoFiltro filtro, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException("page deve ser maior ou igual a zero e size deve estar entre 1 e 50");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataFaturamento", "fatID"));
        var resultado = financeiroRepository.findAll(filtro.toSpecification(), pageable)
                .map(FaturamentoResponse::fromEntity);
        return FaturamentoPaginaResponse.fromPage(resultado);
    }

}
