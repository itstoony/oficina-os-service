package br.com.fiap.os.catalogo.application.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ServicoDTO {

    public record CriarRequest(
            @NotBlank String nome,
            @NotBlank String descricao,
            @NotNull @Positive BigDecimal precoBase
    ) {}

    public record AtualizarRequest(
            @NotBlank String nome,
            @NotBlank String descricao,
            @NotNull @Positive BigDecimal precoBase
    ) {}

    public record Response(
            UUID id,
            String nome,
            String descricao,
            BigDecimal precoBase,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm
    ) {}
}
