package br.com.fiap.os.os.adapter.in.web;

import br.com.fiap.os.os.application.port.in.ItemPecaDTO;
import br.com.fiap.os.os.application.port.in.ItemServicoDTO;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoAdminUseCase;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
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
@RequestMapping("/api/admin/ordens")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço (Admin)", description = "Gestão completa das Ordens de Serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemDeServicoAdminController {

    private final OrdemDeServicoAdminUseCase useCase;

    @PostMapping
    @Operation(summary = "Criar nova Ordem de Serviço")
    public ResponseEntity<OrdemDeServicoDTO.Response> criar(
            @Valid @RequestBody OrdemDeServicoDTO.CriarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Ordens de Serviço ativas")
    public ResponseEntity<List<OrdemDeServicoDTO.Response>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Ordem de Serviço por ID")
    public ResponseEntity<OrdemDeServicoDTO.Response> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PostMapping("/{id}/servicos")
    @Operation(summary = "Adicionar serviço à OS")
    public ResponseEntity<OrdemDeServicoDTO.Response> adicionarServico(
            @PathVariable UUID id,
            @Valid @RequestBody ItemServicoDTO.AdicionarRequest request) {
        return ResponseEntity.ok(useCase.adicionarServico(id, request));
    }

    @DeleteMapping("/{id}/servicos/{itemId}")
    @Operation(summary = "Remover serviço da OS")
    public ResponseEntity<OrdemDeServicoDTO.Response> removerServico(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(useCase.removerServico(id, itemId));
    }

    @PostMapping("/{id}/pecas")
    @Operation(summary = "Adicionar peça à OS")
    public ResponseEntity<OrdemDeServicoDTO.Response> adicionarPeca(
            @PathVariable UUID id,
            @Valid @RequestBody ItemPecaDTO.AdicionarRequest request) {
        return ResponseEntity.ok(useCase.adicionarPeca(id, request));
    }

    @DeleteMapping("/{id}/pecas/{itemId}")
    @Operation(summary = "Remover peça da OS")
    public ResponseEntity<OrdemDeServicoDTO.Response> removerPeca(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(useCase.removerPeca(id, itemId));
    }

    @PostMapping("/{id}/iniciar-diagnostico")
    @Operation(summary = "Iniciar diagnóstico — RECEBIDA → EM_DIAGNOSTICO")
    public ResponseEntity<OrdemDeServicoDTO.Response> iniciarDiagnostico(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.iniciarDiagnostico(id));
    }

    @PostMapping("/{id}/enviar-orcamento")
    @Operation(summary = "Enviar orçamento — EM_DIAGNOSTICO → AGUARDANDO_APROVACAO")
    public ResponseEntity<OrdemDeServicoDTO.Response> enviarOrcamento(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.enviarOrcamento(id));
    }

    @PostMapping("/{id}/iniciar-execucao")
    @Operation(summary = "Iniciar execução — APROVADO → EM_EXECUCAO")
    public ResponseEntity<OrdemDeServicoDTO.Response> iniciarExecucao(
            @PathVariable UUID id,
            @Valid @RequestBody OrdemDeServicoDTO.IniciarExecucaoRequest request) {
        return ResponseEntity.ok(useCase.iniciarExecucao(id, request));
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar — EM_EXECUCAO → FINALIZADA")
    public ResponseEntity<OrdemDeServicoDTO.Response> finalizar(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.finalizar(id));
    }

    @PostMapping("/{id}/entregar")
    @Operation(summary = "Entregar — FINALIZADA → ENTREGUE")
    public ResponseEntity<OrdemDeServicoDTO.Response> entregar(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.entregar(id));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar OS — → CANCELADA")
    public ResponseEntity<OrdemDeServicoDTO.Response> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.cancelar(id));
    }
}
