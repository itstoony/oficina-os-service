package br.com.fiap.os.os.application.port.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemServicoDTO {

    public record AdicionarRequest(
            @NotNull UUID servicoId,
            @NotNull @Positive Integer quantidade,
            String observacao
    ) {}

    public record Response(
            UUID id,
            UUID servicoId,
            String nomeServico,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal,
            String observacao
    ) {}
}
