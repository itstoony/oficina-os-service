package br.com.fiap.os.os.application.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemPecaDTO {

    public record AdicionarRequest(
            @NotNull UUID pecaId,
            @NotBlank String nomePeca,
            @NotNull @Positive Integer quantidade,
            @NotNull @Positive BigDecimal precoUnitario
    ) {}

    public record Response(
            UUID id,
            UUID pecaId,
            String nomePeca,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal
    ) {}
}
