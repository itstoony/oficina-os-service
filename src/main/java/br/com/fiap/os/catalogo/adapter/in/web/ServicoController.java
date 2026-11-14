package br.com.fiap.os.catalogo.adapter.in.web;

import br.com.fiap.os.catalogo.application.port.in.ServicoDTO;
import br.com.fiap.os.catalogo.application.port.in.ServicoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/servicos")
@RequiredArgsConstructor
@Tag(name = "Catálogo de Serviços", description = "Gestão do catálogo de serviços da oficina")
@SecurityRequirement(name = "bearerAuth")
public class ServicoController {

    private final ServicoUseCase useCase;

    @PostMapping
    @Operation(summary = "Criar serviço")
    public ResponseEntity<ServicoDTO.Response> criar(@Valid @RequestBody ServicoDTO.CriarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar serviços")
    public ResponseEntity<List<ServicoDTO.Response>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServicoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServicoDTO.Response> atualizar(@PathVariable UUID id,
                                                          @Valid @RequestBody ServicoDTO.AtualizarRequest request) {
        return ResponseEntity.ok(useCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar serviço")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        useCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
