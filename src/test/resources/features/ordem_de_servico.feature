# language: pt
Funcionalidade: Gestão de Ordens de Serviço
  Como um atendente da oficina
  Quero criar e gerenciar Ordens de Serviço
  Para acompanhar o ciclo de vida dos serviços prestados

  Contexto:
    Dado que existe um cliente cadastrado com nome "João Silva"
    E que existe um veículo cadastrado com placa "ABC1234" do cliente "João Silva"

  Cenário: Criar uma nova Ordem de Serviço com sucesso
    Quando eu criar uma OS para o cliente "João Silva" com o veículo "ABC1234"
    Então a OS deve ser criada com status "RECEBIDA"
    E o número da OS deve ser gerado automaticamente

  Cenário: Avançar OS do status RECEBIDA para EM_DIAGNOSTICO
    Dado que existe uma OS com status "RECEBIDA"
    Quando eu iniciar o diagnóstico da OS
    Então o status da OS deve ser "EM_DIAGNOSTICO"

  Cenário: Cancelar uma OS em diagnóstico
    Dado que existe uma OS com status "EM_DIAGNOSTICO"
    Quando eu cancelar a OS
    Então o status da OS deve ser "CANCELADA"
    E a OS não deve aparecer na listagem de OSs ativas

  Cenário: Tentar transição inválida de status
    Dado que existe uma OS com status "RECEBIDA"
    Quando eu tentar finalizar diretamente a OS
    Então deve ser retornado um erro de regra de negócio
