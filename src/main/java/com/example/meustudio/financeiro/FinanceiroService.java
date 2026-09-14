package com.example.meustudio.financeiro;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.meustudio.financeiro.faturamento.FaturamentoFiltro;
import com.example.meustudio.financeiro.faturamento.FaturamentoPaginaResponse;
import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.shared.NotFoundException;
import com.example.meustudio.procedimento.ProcedimentoService;
import com.example.meustudio.cliente.Cliente;
import com.example.meustudio.cliente.ClienteRepository;

import com.example.meustudio.financeiro.faturamento.Faturamento;
import com.example.meustudio.financeiro.faturamento.FaturamentoRequest;
import com.example.meustudio.financeiro.faturamento.FaturamentoResponse;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;
    private final ProcedimentoService procedimentoService;
    private final ClienteRepository clienteRepository;

    public FinanceiroService(FinanceiroRepository financeiroRepository, ProcedimentoService procedimentoService,
            ClienteRepository clienteRepository) {
        this.financeiroRepository = financeiroRepository;
        this.procedimentoService = procedimentoService;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public FaturamentoResponse criar(FaturamentoRequest request) {
        Faturamento faturamento = new Faturamento();
        atualizarDados(faturamento, request);
        return FaturamentoResponse.fromEntity(financeiroRepository.save(faturamento));
    }

    @Transactional
    public FaturamentoResponse atualizar(java.util.UUID id, FaturamentoRequest request) {
        Faturamento faturamento = financeiroRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lançamento financeiro não encontrado para o id " + id));
        atualizarDados(faturamento, request);
        return FaturamentoResponse.fromEntity(financeiroRepository.save(faturamento));
    }

    @Transactional
    public void excluir(java.util.UUID id) {
        if (!financeiroRepository.existsById(id)) {
            throw new NotFoundException("Lançamento financeiro não encontrado para o id " + id);
        }
        financeiroRepository.deleteById(id);
    }

    private void atualizarDados(Faturamento faturamento, FaturamentoRequest request) {
        var procedimento = procedimentoService.exigirAtivo(request.procedimentoId());
        Cliente clienteCadastrado;
        if (request.clienteId() != null) {
            clienteCadastrado = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente não encontrado para o id " + request.clienteId()));
        } else {
            var correspondencias = clienteRepository.findByNomeNormalizado(request.cliente());
            clienteCadastrado = correspondencias.size() == 1 ? correspondencias.getFirst() : null;
        }
        faturamento.setDataFaturamento(request.data());
        faturamento.setCliente(clienteCadastrado == null ? request.cliente().strip() : clienteCadastrado.getNome());
        faturamento.setClienteCadastrado(clienteCadastrado);
        faturamento.setProcedimento(procedimento.getNome());
        faturamento.setProcedimentoId(procedimento.getId());
        faturamento.setValorBrutoFaturamento(request.valor());
        faturamento.setMeioDePagamento(request.meioDePagamento());

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
