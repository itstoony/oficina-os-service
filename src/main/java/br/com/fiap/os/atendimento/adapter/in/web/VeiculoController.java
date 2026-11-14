package br.com.fiap.os.atendimento.adapter.in.web;

import br.com.fiap.os.atendimento.application.port.in.VeiculoDTO;
import br.com.fiap.os.atendimento.application.port.in.VeiculoUseCase;
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
@RequestMapping("/api/admin/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Gestão de veículos")
@SecurityRequirement(name = "bearerAuth")
public class VeiculoController {

    private final VeiculoUseCase useCase;

    @PostMapping
    @Operation(summary = "Cadastrar veículo")
    public ResponseEntity<VeiculoDTO.Response> cadastrar(@Valid @RequestBody VeiculoDTO.CadastrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar veículos")
    public ResponseEntity<List<VeiculoDTO.Response>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<VeiculoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos por cliente")
    public ResponseEntity<List<VeiculoDTO.Response>> listarPorCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(useCase.listarPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<VeiculoDTO.Response> atualizar(@PathVariable UUID id,
                                                          @Valid @RequestBody VeiculoDTO.AtualizarRequest request) {
        return ResponseEntity.ok(useCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar veículo")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        useCase.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
