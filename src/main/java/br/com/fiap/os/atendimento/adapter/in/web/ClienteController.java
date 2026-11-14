package br.com.fiap.os.atendimento.adapter.in.web;

import br.com.fiap.os.atendimento.application.port.in.ClienteDTO;
import br.com.fiap.os.atendimento.application.port.in.ClienteUseCase;
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
@RequestMapping("/api/admin/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestão de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteUseCase useCase;

    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    public ResponseEntity<ClienteDTO.Response> cadastrar(@Valid @RequestBody ClienteDTO.CadastrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar clientes")
    public ResponseEntity<List<ClienteDTO.Response>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    public ResponseEntity<ClienteDTO.Response> atualizar(@PathVariable UUID id,
                                                          @Valid @RequestBody ClienteDTO.AtualizarRequest request) {
        return ResponseEntity.ok(useCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cliente")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        useCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
