package com.example.meustudio.cliente;

import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.meustudio.financeiro.FinanceiroRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final FinanceiroRepository financeiroRepository;

    public ClienteService(ClienteRepository clienteRepository, FinanceiroRepository financeiroRepository) {
        this.clienteRepository = clienteRepository;
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional(readOnly = true)
    public ClientePaginaResponse listar(ClienteFiltro filtro, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException("page deve ser maior ou igual a zero e size deve estar entre 1 e 50");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "criadoEm", "id"));
        var resultado = clienteRepository.findAll(filtro.toSpecification(), pageable)
                .map(ClienteResponse::fromEntity);
        return ClientePaginaResponse.fromPage(resultado);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado para o id " + id));
        return ClienteResponse.fromEntity(cliente);
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        validarEmailDuplicado(request.email(), null);

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());

        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado para o id " + id));

        validarEmailDuplicado(request.email(), id);

        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado para o id " + id);
        }
        if (financeiroRepository.existsByClienteCadastradoId(id)) {
            throw new BusinessException("Cliente possui lançamentos financeiros. Altere ou exclua esses lançamentos na aba Financeiro antes de excluir o cliente.");
        }
        clienteRepository.deleteById(id);
    }

    private void validarEmailDuplicado(String email, Long idAtual) {
        if (email == null) {
            return;
        }

        clienteRepository.findByEmail(email).ifPresent(clienteExistente -> {
            boolean mesmoRegistro = idAtual != null && clienteExistente.getId().equals(idAtual);
            if (!mesmoRegistro) {
                throw new BusinessException("Já existe cliente cadastrado com esse email");
            }
        });
    }
}
