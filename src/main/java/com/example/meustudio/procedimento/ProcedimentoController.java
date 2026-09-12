package com.example.meustudio.procedimento;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoController {
    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) { this.service = service; }

    public record Pagina(List<ProcedimentoResponse> content, int page, int size, long totalElements, int totalPages) {
        static Pagina fromPage(Page<ProcedimentoResponse> page) {
            return new Pagina(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    public record StatusRequest(@NotNull(message = "Situação é obrigatória") Boolean ativo) {}

    @GetMapping
    public Pagina listar(@RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Pagina.fromPage(service.listar(nome, categoria, ativo, page, size));
    }

    @GetMapping("/categorias")
    public List<String> categorias() { return service.categorias(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcedimentoResponse criar(@Valid @RequestBody ProcedimentoRequest request) { return service.criar(request); }

    @PutMapping("/{id}")
    public ProcedimentoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProcedimentoRequest request) {
        return service.atualizar(id, request);
    }

    @PutMapping("/{id}/status")
    public ProcedimentoResponse status(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return service.alterarStatus(id, request.ativo());
    }
}
