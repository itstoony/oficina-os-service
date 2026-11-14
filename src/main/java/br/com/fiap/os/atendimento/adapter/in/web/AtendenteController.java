package br.com.fiap.os.atendimento.adapter.in.web;

import br.com.fiap.os.atendimento.application.port.in.AtendenteDTO;
import br.com.fiap.os.atendimento.application.port.in.AtendenteUseCase;
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
@RequestMapping("/api/admin/atendentes")
@RequiredArgsConstructor
@Tag(name = "Atendentes", description = "Gestão de atendentes")
@SecurityRequirement(name = "bearerAuth")
public class AtendenteController {

    private final AtendenteUseCase useCase;

    @PostMapping
    @Operation(summary = "Cadastrar atendente")
    public ResponseEntity<AtendenteDTO.Response> cadastrar(@Valid @RequestBody AtendenteDTO.CadastrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar atendentes")
    public ResponseEntity<List<AtendenteDTO.Response>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar atendente por ID")
    public ResponseEntity<AtendenteDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar atendente")
    public ResponseEntity<AtendenteDTO.Response> atualizar(@PathVariable UUID id,
                                                            @Valid @RequestBody AtendenteDTO.AtualizarRequest request) {
        return ResponseEntity.ok(useCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar atendente")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        useCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
