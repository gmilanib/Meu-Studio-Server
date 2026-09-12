package com.example.meustudio.procedimento;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.shared.NotFoundException;

@Service
public class ProcedimentoService {
    private final ProcedimentoRepository repository;

    public ProcedimentoService(ProcedimentoRepository repository) { this.repository = repository; }

    public Page<ProcedimentoResponse> listar(String nome, String categoria, Boolean ativo, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException("page deve ser maior ou igual a zero e size deve estar entre 1 e 50");
        }
        return repository.findAll((root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (nome != null && !nome.isBlank()) {
                String trecho = nome.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                filtros.add(cb.like(cb.lower(root.get("nome")), "%" + trecho + "%", '\\'));
            }
            if (categoria != null && !categoria.isBlank()) {
                filtros.add(cb.equal(root.get("categoria"), categoria.trim()));
            }
            if (ativo != null) filtros.add(cb.equal(root.get("ativo"), ativo));
            return cb.and(filtros.toArray(Predicate[]::new));
        }, PageRequest.of(page, size, Sort.by(Sort.Order.asc("nome").ignoreCase(), Sort.Order.asc("id"))))
                .map(ProcedimentoResponse::fromEntity);
    }

    public List<String> categorias() { return repository.categorias(); }

    @Transactional
    public ProcedimentoResponse criar(ProcedimentoRequest request) {
        verificarNome(request.nome(), null);
        Procedimento procedimento = new Procedimento();
        procedimento.atualizar(request);
        return salvar(procedimento);
    }

    @Transactional
    public ProcedimentoResponse atualizar(UUID id, ProcedimentoRequest request) {
        Procedimento procedimento = buscar(id);
        verificarNome(request.nome(), id);
        procedimento.atualizar(request);
        return salvar(procedimento);
    }

    @Transactional
    public ProcedimentoResponse alterarStatus(UUID id, boolean ativo) {
        Procedimento procedimento = buscar(id);
        procedimento.setAtivo(ativo);
        return salvar(procedimento);
    }

    @Transactional
    public Procedimento exigirAtivo(UUID id) {
        if (id == null) throw new BusinessException("Selecione um procedimento ativo");
        Procedimento procedimento = buscar(id);
        if (!procedimento.isAtivo()) throw new BusinessException("O procedimento está inativo. Selecione outro procedimento.");
        return procedimento;
    }

    private Procedimento buscar(UUID id) {
        return repository.buscarParaAtualizar(id)
                .orElseThrow(() -> new NotFoundException("Procedimento não encontrado"));
    }

    private void verificarNome(String nome, UUID id) {
        boolean repetido = id == null ? repository.existsByNomeIgnoreCase(nome.trim())
                : repository.existsByNomeIgnoreCaseAndIdNot(nome.trim(), id);
        if (repetido) throw new BusinessException("Já existe um procedimento com esse nome, inclusive entre os inativos");
    }

    private ProcedimentoResponse salvar(Procedimento procedimento) {
        try {
            return ProcedimentoResponse.fromEntity(repository.saveAndFlush(procedimento));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("Não foi possível salvar. Verifique se o nome já está cadastrado e se os dados são válidos.");
        }
    }
}
