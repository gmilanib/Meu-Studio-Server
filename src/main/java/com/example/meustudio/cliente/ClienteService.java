package com.example.meustudio.cliente;

import com.example.meustudio.shared.BusinessException;
import com.example.meustudio.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(ClienteResponse::fromEntity).toList();
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
        clienteRepository.deleteById(id);
    }

    private void validarEmailDuplicado(String email, Long idAtual) {
        clienteRepository.findByEmail(email).ifPresent(clienteExistente -> {
            boolean mesmoRegistro = idAtual != null && clienteExistente.getId().equals(idAtual);
            if (!mesmoRegistro) {
                throw new BusinessException("Já existe cliente cadastrado com esse email");
            }
        });
    }
}
