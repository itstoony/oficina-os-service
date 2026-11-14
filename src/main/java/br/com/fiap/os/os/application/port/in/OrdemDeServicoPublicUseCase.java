package br.com.fiap.os.os.application.port.in;

public interface OrdemDeServicoPublicUseCase {
    OrdemDeServicoDTO.StatusPublicoResponse buscarStatusPublico(String numero);
    OrdemDeServicoDTO.StatusPublicoResponse aprovar(String numero);
    OrdemDeServicoDTO.StatusPublicoResponse recusar(String numero);
}
