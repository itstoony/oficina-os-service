package br.com.fiap.os.catalogo.application.port.in;

public class RelatorioDTO {

    public record TempoMedioResponse(
            int totalOrdens,
            long mediaMinutos,
            String mediaFormatada
    ) {}
}
