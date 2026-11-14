package br.com.fiap.os.bdd.steps;

import br.com.fiap.os.atendimento.application.port.in.ClienteDTO;
import br.com.fiap.os.atendimento.application.port.in.ClienteUseCase;
import br.com.fiap.os.atendimento.application.port.in.VeiculoDTO;
import br.com.fiap.os.atendimento.application.port.in.VeiculoUseCase;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoAdminUseCase;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
import br.com.fiap.os.shared.exception.RegraDeNegocioException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrdemDeServicoSteps {

    @Autowired
    private ClienteUseCase clienteUseCase;

    @Autowired
    private VeiculoUseCase veiculoUseCase;

    @Autowired
    private OrdemDeServicoAdminUseCase ordemDeServicoUseCase;

    private UUID clienteId;
    private UUID veiculoId;
    private OrdemDeServicoDTO.Response ordemAtual;
    private Exception excecaoCapturada;

    @Dado("que existe um cliente cadastrado com nome {string}")
    public void criarCliente(String nome) {
        ClienteDTO.Response cliente = clienteUseCase.cadastrar(new ClienteDTO.CadastrarRequest(
                nome, nome.toLowerCase().replace(" ", "") + "@email.com",
                "11999999999", "529.982.247-25"
        ));
        clienteId = cliente.id();
    }

    @E("que existe um veículo cadastrado com placa {string} do cliente {string}")
    public void criarVeiculo(String placa, String nomeCliente) {
        VeiculoDTO.Response veiculo = veiculoUseCase.cadastrar(new VeiculoDTO.CadastrarRequest(
                "Toyota", "Corolla", 2020, "Prata", placa, clienteId
        ));
        veiculoId = veiculo.id();
    }

    @Dado("que existe uma OS com status {string}")
    public void criarOsComStatus(String status) {
        ordemAtual = ordemDeServicoUseCase.criar(new OrdemDeServicoDTO.CriarRequest(
                clienteId, veiculoId, null, "OS de teste"
        ));
        if ("EM_DIAGNOSTICO".equals(status)) {
            ordemAtual = ordemDeServicoUseCase.iniciarDiagnostico(ordemAtual.id());
        }
    }

    @Quando("eu criar uma OS para o cliente {string} com o veículo {string}")
    public void criarOs(String nomeCliente, String placa) {
        ordemAtual = ordemDeServicoUseCase.criar(new OrdemDeServicoDTO.CriarRequest(
                clienteId, veiculoId, null, null
        ));
    }

    @Quando("eu iniciar o diagnóstico da OS")
    public void iniciarDiagnostico() {
        ordemAtual = ordemDeServicoUseCase.iniciarDiagnostico(ordemAtual.id());
    }

    @Quando("eu cancelar a OS")
    public void cancelarOs() {
        ordemAtual = ordemDeServicoUseCase.cancelar(ordemAtual.id());
    }

    @Quando("eu tentar finalizar diretamente a OS")
    public void tentarFinalizarDiretamente() {
        try {
            ordemDeServicoUseCase.finalizar(ordemAtual.id());
        } catch (RegraDeNegocioException e) {
            excecaoCapturada = e;
        }
    }

    @Entao("a OS deve ser criada com status {string}")
    public void verificarStatusCriacao(String statusEsperado) {
        assertThat(ordemAtual).isNotNull();
        assertThat(ordemAtual.status()).isEqualTo(statusEsperado);
    }

    @E("o número da OS deve ser gerado automaticamente")
    public void verificarNumeroGerado() {
        assertThat(ordemAtual.numero()).isNotBlank();
        assertThat(ordemAtual.numero()).startsWith("OS-");
    }

    @Entao("o status da OS deve ser {string}")
    public void verificarStatus(String statusEsperado) {
        assertThat(ordemAtual.status()).isEqualTo(statusEsperado);
    }

    @E("a OS não deve aparecer na listagem de OSs ativas")
    public void verificarOsNaoListada() {
        boolean encontrada = ordemDeServicoUseCase.listar().stream()
                .anyMatch(os -> os.id().equals(ordemAtual.id()));
        assertThat(encontrada).isFalse();
    }

    @Entao("deve ser retornado um erro de regra de negócio")
    public void verificarErroRegraDeNegocio() {
        assertThat(excecaoCapturada).isNotNull().isInstanceOf(RegraDeNegocioException.class);
    }
}
