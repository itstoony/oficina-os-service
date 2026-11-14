package br.com.fiap.os.os.adapter.in.web;

import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoPublicUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/ordens")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço (Público)", description = "Consulta e aprovação de orçamento pelo cliente")
public class OrdemDeServicoPublicController {

    private final OrdemDeServicoPublicUseCase useCase;

    @GetMapping("/{numero}/status")
    @Operation(summary = "Consultar status da OS pelo número")
    public ResponseEntity<OrdemDeServicoDTO.StatusPublicoResponse> buscarStatus(@PathVariable String numero) {
        return ResponseEntity.ok(useCase.buscarStatusPublico(numero));
    }

    @PostMapping("/{numero}/aprovar")
    @Operation(summary = "Aprovar orçamento da OS")
    public ResponseEntity<OrdemDeServicoDTO.StatusPublicoResponse> aprovar(@PathVariable String numero) {
        return ResponseEntity.ok(useCase.aprovar(numero));
    }

    @PostMapping("/{numero}/recusar")
    @Operation(summary = "Recusar orçamento da OS")
    public ResponseEntity<OrdemDeServicoDTO.StatusPublicoResponse> recusar(@PathVariable String numero) {
        return ResponseEntity.ok(useCase.recusar(numero));
    }
}
