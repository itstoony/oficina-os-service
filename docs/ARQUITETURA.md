# Documentação de Arquitetura — OS Service
## FIAP Pós-Tech Software Architecture — Tech Challenge Fase 4

---

## Sumário

1. [Visão Geral do Serviço](#1-visão-geral-do-serviço)
2. [Arquitetura da Fase 4 — Microsserviços](#2-arquitetura-da-fase-4--microsserviços)
3. [Diagrama de Componentes do Serviço](#3-diagrama-de-componentes-do-serviço)
4. [Diagrama de Sequência — Saga Happy Path](#4-diagrama-de-sequência--saga-happy-path)
5. [Diagrama de Sequência — Saga Rollback](#5-diagrama-de-sequência--saga-rollback)
6. [Diagrama ER](#6-diagrama-er)
7. [Arquitetura Hexagonal](#7-arquitetura-hexagonal)
8. [Eventos Publicados e Consumidos](#8-eventos-publicados-e-consumidos)
9. [Justificativa do Banco de Dados](#9-justificativa-do-banco-de-dados)
10. [ADRs](#10-adrs)

---

## 1. Visão Geral do Serviço

O **OS Service** é o microsserviço central da plataforma de oficina mecânica. É responsável por gerenciar o ciclo de vida completo das Ordens de Serviço (OS), além de manter o cadastro de clientes, veículos, atendentes e catálogo de serviços.

| Atributo | Valor |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5.3 |
| Banco de Dados | PostgreSQL 16 |
| Mensageria | RabbitMQ (produtor e consumidor) |
| Porta | 8080 |
| Autenticação | JWT HS384 (admin) |

### Responsabilidades

- Abertura, atualização e consulta de Ordens de Serviço
- Cadastro de clientes, veículos e atendentes
- Catálogo de serviços e relatórios
- Coordenação do Saga Pattern via eventos RabbitMQ
- Endpoint público para aprovação/recusa de OS pelo cliente

---

## 2. Arquitetura da Fase 4 — Microsserviços

Diagrama completo da comunicação entre os 3 microsserviços via RabbitMQ (Exchange: `oficina.events`).

```mermaid
graph TB
    subgraph Cliente["Atores Externos"]
        ADM[Admin / Atendente]
        CLI[Cliente]
    end

    subgraph RMQ["RabbitMQ — oficina.events (Topic Exchange)"]
        direction LR
        E1([os.aberta])
        E2([os.aprovada])
        E3([os.cancelada])
        E4([orcamento.gerado])
        E5([pagamento.confirmado])
        E6([pagamento.recusado])
        E7([estoque.reservado])
        E8([estoque.indisponivel])
        E9([execucao.finalizada])
    end

    subgraph OS["OS Service — :8080\nPostgreSQL"]
        OS_API[REST API]
        OS_CON[Event Consumer]
        OS_PUB[Event Publisher]
    end

    subgraph BILLING["Billing Service — :8082\nMongoDB"]
        B_API[REST API]
        B_CON[Event Consumer]
        B_PUB[Event Publisher]
    end

    subgraph EXEC["Execucao Service — :8081\nPostgreSQL"]
        EX_API[REST API]
        EX_CON[Event Consumer]
        EX_PUB[Event Publisher]
    end

    ADM -->|JWT Bearer| OS_API
    CLI -->|endpoint público| OS_API

    OS_PUB -->|publica| E1
    OS_PUB -->|publica| E2
    OS_PUB -->|publica| E3

    E1 -->|consome| B_CON
    E2 -->|consome| EX_CON
    E3 -->|consome| EX_CON

    B_PUB -->|publica| E4
    B_PUB -->|publica| E5
    B_PUB -->|publica| E6

    E4 -->|consome| OS_CON
    E5 -->|consome| OS_CON
    E5 -->|consome| EX_CON
    E6 -->|consome| OS_CON

    EX_PUB -->|publica| E7
    EX_PUB -->|publica| E8
    EX_PUB -->|publica| E9

    E7 -->|consome| OS_CON
    E8 -->|consome| OS_CON
    E9 -->|consome| OS_CON
```

### Fluxo do Saga Pattern (Coreografado)

```mermaid
flowchart LR
    A([OS Aberta]) -->|os.aberta| B([Billing\ngera orçamento])
    B -->|orcamento.gerado| C([OS: AGUARDANDO\nAPROVACAO])
    C -->|cliente aprova| D([OS: APROVADO\nos.aprovada])
    D -->|os.aprovada| E{Execucao\nverifica estoque}
    E -->|estoque ok\nestoque.reservado| F([Billing\nprocessa pagamento])
    E -->|sem estoque\nestoque.indisponivel| Z([OS: CANCELADA\nrollback])
    F -->|pagamento.confirmado| G([OS: EM_EXECUCAO])
    F -->|pagamento.recusado| C
    G -->|execucao.finalizada| H([OS: FINALIZADA])
    H -->|admin entrega| I([OS: ENTREGUE])

    style Z fill:#ff6b6b,color:#fff
    style I fill:#51cf66,color:#fff
```

---

## 3. Diagrama de Componentes do Serviço

```mermaid
graph TB
    subgraph EKS["AWS EKS — oficina-cluster"]
        subgraph OS_SVC["OS Service Pod"]
            CTRL["Controllers\n(REST Adapters)"]
            SVC["Application Services\n(Use Cases)"]
            DOM["Domain Model\n(Entities + Value Objects)"]
            REP["Repository Adapters\n(JPA)"]
            MSG_IN["Messaging Consumer\n(RabbitMQ)"]
            MSG_OUT["Messaging Publisher\n(RabbitMQ)"]
        end

        subgraph DB["os-db Pod"]
            PG["PostgreSQL 16\nos_service"]
        end

        LB["LoadBalancer\n:8080"]
    end

    subgraph RMQ["RabbitMQ Pod"]
        QUEUE["os.eventos.queue"]
    end

    subgraph GHA["GitHub Actions"]
        PIPE["CI/CD Pipeline\nbuild → test → sonar → docker → deploy"]
    end

    LB --> CTRL
    CTRL --> SVC
    SVC --> DOM
    SVC --> REP
    SVC --> MSG_OUT
    REP --> PG
    MSG_IN --> SVC
    QUEUE --> MSG_IN
    MSG_OUT --> QUEUE
    PIPE -->|kubectl rollout restart| OS_SVC
```

---

## 4. Diagrama de Sequência — Saga Happy Path

```mermaid
sequenceDiagram
    actor Admin
    actor Cliente
    participant OS as OS Service
    participant RMQ as RabbitMQ
    participant Billing as Billing Service
    participant Exec as Execucao Service

    Admin->>OS: POST /api/auth/login
    OS-->>Admin: JWT

    Admin->>OS: POST /api/admin/ordens
    OS->>OS: Cria OS (status: RECEBIDA)
    OS->>RMQ: publica os.aberta
    RMQ->>Billing: consome os.aberta
    Billing->>Billing: Cria orçamento (MongoDB)
    Billing->>RMQ: publica orcamento.gerado
    RMQ->>OS: consome orcamento.gerado
    OS->>OS: Atualiza valorTotal

    Admin->>OS: POST /api/admin/ordens/{id}/iniciar-diagnostico
    OS->>OS: status → EM_DIAGNOSTICO

    Admin->>OS: POST /api/admin/ordens/{id}/enviar-orcamento
    OS->>OS: status → AGUARDANDO_APROVACAO

    Cliente->>OS: POST /api/public/ordens/{numero}/aprovar
    OS->>OS: status → APROVADO (commit)
    OS->>RMQ: publica os.aprovada (após commit)
    RMQ->>Exec: consome os.aprovada
    Exec->>Exec: Reserva estoque
    Exec->>RMQ: publica estoque.reservado
    RMQ->>OS: consome estoque.reservado

    Admin->>Billing: POST /api/billing/pagamentos
    Billing->>Billing: Cria preferência Mercado Pago
    Admin->>Billing: POST /api/billing/pagamentos/{osId}/confirmar-teste
    Billing->>RMQ: publica pagamento.confirmado
    RMQ->>OS: consome pagamento.confirmado
    OS->>OS: status → EM_EXECUCAO
    RMQ->>Exec: consome pagamento.confirmado
    Exec->>Exec: Entra na fila de execução

    Admin->>Exec: POST /api/execucao/fila/{osId}/iniciar
    Admin->>Exec: POST /api/execucao/fila/{osId}/finalizar
    Exec->>RMQ: publica execucao.finalizada
    RMQ->>OS: consome execucao.finalizada
    OS->>OS: status → FINALIZADA

    Admin->>OS: POST /api/admin/ordens/{id}/entregar
    OS->>OS: status → ENTREGUE
```

---

## 5. Diagrama de Sequência — Saga Rollback

```mermaid
sequenceDiagram
    actor Admin
    actor Cliente
    participant OS as OS Service
    participant RMQ as RabbitMQ
    participant Exec as Execucao Service

    Admin->>OS: POST /api/admin/ordens (nova OS)
    OS->>RMQ: publica os.aberta

    Admin->>OS: Adiciona peça, envia orçamento
    OS->>OS: status → AGUARDANDO_APROVACAO

    Cliente->>OS: POST /api/public/ordens/{numero}/aprovar
    OS->>OS: status → APROVADO (commit)
    OS->>RMQ: publica os.aprovada (após commit)

    RMQ->>Exec: consome os.aprovada
    Exec->>Exec: Verifica estoque
    note over Exec: Estoque insuficiente!
    Exec->>RMQ: publica estoque.indisponivel

    RMQ->>OS: consome estoque.indisponivel
    OS->>OS: status → CANCELADA
    note over OS: Saga compensado com sucesso
```

---

## 6. Diagrama ER

```mermaid
erDiagram
    CLIENTES {
        uuid id PK
        string nome
        string email
        string telefone
        string documento_numero
        string documento_tipo
        timestamp criado_em
        timestamp atualizado_em
    }

    VEICULOS {
        uuid id PK
        string marca
        string modelo
        int ano
        string cor
        string placa_valor
        uuid cliente_id FK
        timestamp criado_em
        timestamp atualizado_em
    }

    ATENDENTES {
        uuid id PK
        string nome
        string email
        string telefone
        timestamp criado_em
        timestamp atualizado_em
    }

    SERVICOS {
        uuid id PK
        string nome
        string descricao
        decimal preco
        boolean ativo
        timestamp criado_em
        timestamp atualizado_em
    }

    ORDENS_DE_SERVICO {
        uuid id PK
        string numero
        string status
        decimal valor_total
        string observacoes
        boolean ativo
        timestamp data_abertura
        timestamp data_inicio_execucao
        timestamp data_fim_execucao
        uuid cliente_id FK
        uuid veiculo_id FK
        uuid atendente_id FK
        timestamp criado_em
        timestamp atualizado_em
    }

    ITENS_SERVICO {
        uuid id PK
        uuid os_id FK
        uuid servico_id FK
        string nome_servico
        int quantidade
        decimal preco_unitario
        string observacao
    }

    ITENS_PECA {
        uuid id PK
        uuid os_id FK
        uuid peca_id
        string nome_peca
        int quantidade
        decimal preco_unitario
    }

    USUARIOS {
        uuid id PK
        string login
        string nome
        string senha
    }

    CLIENTES ||--o{ VEICULOS : "possui"
    CLIENTES ||--o{ ORDENS_DE_SERVICO : "solicita"
    VEICULOS ||--o{ ORDENS_DE_SERVICO : "referenciado em"
    ATENDENTES ||--o{ ORDENS_DE_SERVICO : "atende"
    ORDENS_DE_SERVICO ||--o{ ITENS_SERVICO : "contém"
    ORDENS_DE_SERVICO ||--o{ ITENS_PECA : "utiliza"
    SERVICOS ||--o{ ITENS_SERVICO : "referenciado em"
```

---

## 7. Arquitetura Hexagonal

```mermaid
graph LR
    subgraph Adapters_In["Adapters — Entrada"]
        REST["REST Controllers\n(Spring MVC)"]
        MSG_IN["Event Consumer\n(RabbitMQ Listener)"]
    end

    subgraph Application["Application Core"]
        subgraph Ports_In["Ports — Entrada"]
            UC["Use Cases\n(interfaces)"]
        end
        subgraph Domain["Domain"]
            ENT["Entities\nOrdemDeServico\nCliente\nVeiculo\nAtendente"]
            VO["Value Objects\nStatusOS\nPlaca\nDocumento"]
        end
        subgraph Ports_Out["Ports — Saída"]
            REPO_P["Repository Ports\n(interfaces)"]
            EVENT_P["Event Publisher Port\n(interface)"]
            EMAIL_P["Email Port\n(interface)"]
        end
        SVC["Services\nOrdemDeServicoService\nClienteService\nVeiculoService\nAtendenteService"]
    end

    subgraph Adapters_Out["Adapters — Saída"]
        JPA["JPA Adapters\n(Spring Data)"]
        MSG_OUT["RabbitMQ Publisher"]
        EMAIL["Email Adapter\n(JavaMailSender)"]
    end

    subgraph Infra["Infraestrutura"]
        PG[(PostgreSQL)]
        RMQ[(RabbitMQ)]
        SMTP[(SMTP)]
    end

    REST --> UC
    MSG_IN --> UC
    UC --> SVC
    SVC --> ENT
    SVC --> VO
    SVC --> REPO_P
    SVC --> EVENT_P
    SVC --> EMAIL_P
    REPO_P --> JPA
    EVENT_P --> MSG_OUT
    EMAIL_P --> EMAIL
    JPA --> PG
    MSG_OUT --> RMQ
    EMAIL --> SMTP
```

**Regras de dependência:**
- `domain` — zero imports de Spring, JPA ou qualquer framework
- `application/service` — depende apenas de `domain` e interfaces `port`
- `adapter` — implementações concretas, depende de `application`

---

## 8. Eventos Publicados e Consumidos

### Publica

| Routing Key | Trigger | Payload principal |
|---|---|---|
| `os.aberta` | POST /api/admin/ordens | osId, numero, clienteId, itensPeca[] |
| `os.aprovada` | POST /api/public/ordens/{numero}/aprovar (após commit) | osId, numero, pecas[] |
| `os.cancelada` | POST /api/admin/ordens/{id}/cancelar ou recusar | osId, numero |

### Consome

| Routing Key | Origem | Ação |
|---|---|---|
| `orcamento.gerado` | Billing Service | Atualiza valorTotal da OS |
| `estoque.reservado` | Execucao Service | (confirmação — sem mudança de status) |
| `estoque.indisponivel` | Execucao Service | Transiciona OS para CANCELADA |
| `pagamento.confirmado` | Billing Service | Transiciona OS para EM_EXECUCAO |
| `pagamento.recusado` | Billing Service | Transiciona OS para CANCELADA |
| `execucao.finalizada` | Execucao Service | Transiciona OS para FINALIZADA |

---

## 9. Justificativa do Banco de Dados

**PostgreSQL** foi escolhido para o OS Service pelos seguintes motivos:

- O modelo de dados é fortemente relacional: OS → itens → cliente → veículo → atendente
- Necessidade de transações ACID para garantir consistência no ciclo de vida da OS
- Consultas com JOINs entre entidades (relatório de tempo médio, listagem com filtros)
- Migrações gerenciadas pelo Flyway com versionamento controlado

---

## 10. ADRs

### ADR-001: Saga Pattern Coreografado

**Status:** Aceito

**Contexto:** O fluxo de uma OS envolve 3 serviços independentes (OS, Billing, Execucao). É necessário coordenar o fluxo transacional com rollback seguro em caso de falha.

**Decisão:** Implementar o Saga Pattern na variante **coreografada**, onde cada serviço reage a eventos e publica seus próprios resultados, sem orquestrador central.

**Justificativa:**
- Elimina ponto único de falha (sem orquestrador central)
- Cada serviço é autônomo e desacoplado
- Mais resiliente a falhas pontuais
- Alinha com a arquitetura orientada a eventos já adotada

**Consequências:**
- Rastreabilidade do fluxo requer correlação de logs entre serviços
- Necessidade de idempotência nos consumidores

---

### ADR-002: Publicar `os.aprovada` Após Commit da Transação

**Status:** Aceito

**Contexto:** Race condition identificada em produção: o evento `os.aprovada` era publicado **dentro** da transação do método `aprovar()`, antes do commit. O Execucao Service processava o evento e publicava `estoque.indisponivel` antes do commit da transação do OS Service, fazendo com que o status cancelado fosse sobrescrito pelo commit da transação original.

**Decisão:** Registrar o publish do evento no `TransactionSynchronizationManager.afterCommit()`, garantindo que o evento só seja publicado após o commit confirmado.

**Consequências:**
- O Saga é robusto a race conditions de timing
- Em contextos sem transação ativa (testes unitários), o evento é publicado imediatamente como fallback
