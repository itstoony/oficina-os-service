# oficina-os-service

**FIAP Pós-Tech Software Architecture — Tech Challenge Fase 4**

Microsserviço responsável pelo ciclo de vida completo das **Ordens de Serviço**, além de Clientes, Veículos, Atendentes e Catálogo de Serviços.

---

## Arquitetura

```
┌──────────────────────────────────────────────────────┐
│                   os-service (:8080)                 │
│                                                      │
│  adapter/in/web  →  application/service  →  domain  │
│       ↕                    ↕                         │
│  adapter/out/persistence  adapter/out/messaging      │
│       ↕                    ↕                         │
│    PostgreSQL           RabbitMQ                     │
└──────────────────────────────────────────────────────┘
```

**Padrão:** Arquitetura Hexagonal (Ports & Adapters) + DDD

**Banco:** PostgreSQL (relacional — ciclo de vida da OS exige transações ACID)

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.3 |
| PostgreSQL | 16 |
| RabbitMQ | 3 |
| Flyway | — |
| Cucumber (BDD) | 7.18.0 |
| JaCoCo | 80% mínimo |

---

## Saga Pattern — Coreografado

Este serviço é o **coordenador implícito** do Saga Pattern coreografado. Cada transição de status da OS é acionada por um evento recebido ou por uma ação do admin/cliente.

**Justificativa da escolha coreografada:** Evita acoplamento central (sem orquestrador), cada serviço reage a eventos e publica seus próprios resultados, tornando o sistema resiliente a falhas pontuais.

### Fluxo happy path

```
Admin cria OS
  → publica: os.aberta

Billing recebe os.aberta
  → gera orçamento → publica: orcamento.gerado

OS Service recebe orcamento.gerado
  → status: AGUARDANDO_APROVACAO

Admin envia orçamento ao cliente
  → publica: orcamento.solicitado (e-mail enviado)

Cliente aprova via endpoint público
  → publica: os.aprovada

Execucao recebe os.aprovada
  → reserva estoque → publica: estoque.reservado

OS Service recebe estoque.reservado
  → status: APROVADO

Admin inicia pagamento no Billing
  → webhook Mercado Pago → publica: pagamento.confirmado

OS Service recebe pagamento.confirmado
  → status: EM_EXECUCAO

Execucao finaliza → publica: execucao.finalizada

OS Service recebe execucao.finalizada
  → status: FINALIZADA → ENTREGUE
```

### Rollback (compensação)

| Evento | Ação compensatória |
|--------|--------------------|
| `estoque.indisponivel` | OS → CANCELADA, Billing cancela orçamento |
| `pagamento.recusado` | OS → AGUARDANDO_APROVACAO (retenta pagamento) |
| `os.cancelada` | Execucao libera reservas de estoque |

---

## Endpoints

### Autenticação
| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login admin → retorna JWT |

### Ordens de Serviço (requerem JWT)
| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/admin/ordens` | Abrir OS |
| GET | `/api/admin/ordens` | Listar OSs ativas |
| GET | `/api/admin/ordens/{id}` | Buscar OS por ID |
| POST | `/api/admin/ordens/{id}/iniciar-diagnostico` | RECEBIDA → EM_DIAGNOSTICO |
| POST | `/api/admin/ordens/{id}/enviar-orcamento` | EM_DIAGNOSTICO → AGUARDANDO_APROVACAO |
| POST | `/api/admin/ordens/{id}/iniciar-execucao` | APROVADO → EM_EXECUCAO |
| POST | `/api/admin/ordens/{id}/finalizar` | EM_EXECUCAO → FINALIZADA |
| POST | `/api/admin/ordens/{id}/entregar` | FINALIZADA → ENTREGUE |
| POST | `/api/admin/ordens/{id}/cancelar` | Cancelar OS |

### Endpoints públicos (sem autenticação)
| Método | Path | Descrição |
|--------|------|-----------|
| GET | `/api/public/ordens/{numero}/status` | Status público da OS |
| POST | `/api/public/ordens/{numero}/aprovar` | Cliente aprova orçamento |
| POST | `/api/public/ordens/{numero}/recusar` | Cliente recusa orçamento |

### Clientes, Veículos, Atendentes, Serviços (requerem JWT)
```
POST/GET        /api/admin/clientes
GET/PUT/DELETE  /api/admin/clientes/{id}
POST/GET        /api/admin/veiculos
GET/PUT/DELETE  /api/admin/veiculos/{id}
POST/GET        /api/admin/atendentes
GET/PUT/DELETE  /api/admin/atendentes/{id}
POST/GET        /api/admin/servicos
GET/PUT/DELETE  /api/admin/servicos/{id}
GET             /api/admin/relatorios/tempo-medio
```

---

## Eventos RabbitMQ

**Exchange:** `oficina.events` (TopicExchange)

### Publica
| Routing Key | Quando |
|-------------|--------|
| `os.aberta` | OS criada |
| `os.aprovada` | Cliente aprova orçamento |
| `os.cancelada` | OS cancelada ou recusada pelo cliente |
| `orcamento.solicitado` | Admin envia orçamento para o cliente |

### Consome
| Routing Key | Ação |
|-------------|------|
| `orcamento.gerado` | Atualiza valor total da OS |
| `pagamento.confirmado` | Libera OS → EM_EXECUCAO |
| `pagamento.recusado` | Retorna OS → AGUARDANDO_APROVACAO |
| `estoque.reservado` | Avança OS → APROVADO |
| `estoque.indisponivel` | Cancela OS por falta de estoque |
| `execucao.finalizada` | Marca OS como FINALIZADA |

---

## Testes

### Unitários (JUnit 5 + Mockito)
```bash
./mvnw test
```

### Cobertura JaCoCo (mínimo 80%)
```bash
./mvnw verify
# Relatório: target/site/jacoco/index.html
```

> **Evidência de cobertura:** adicione aqui o print do relatório JaCoCo após rodar `./mvnw verify`

### BDD (Cucumber)
```bash
./mvnw test -Dtest=CucumberRunnerTest
# Feature: src/test/resources/features/ordem_de_servico.feature
```

O fluxo BDD cobre: abertura da OS → diagnóstico → envio de orçamento → aprovação → execução → finalização.

---

## Executar localmente

```bash
# Subir PostgreSQL + RabbitMQ
docker compose up -d

# Rodar aplicação
./mvnw spring-boot:run

# Swagger UI
http://localhost:8080/swagger-ui.html
```

---

## CI/CD

Pipeline: **build-test → sonar → docker → deploy**

O job `docker` só executa se `build-test` **e** `sonar` passarem.

Secrets necessários: `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `KUBECONFIG`, `SONAR_TOKEN`

---

## Infraestrutura Kubernetes

```
k8s/
├── namespace.yaml   — namespace oficina
├── secret.yaml      — credenciais (DB, JWT, e-mail)
├── configmap.yaml   — variáveis de ambiente
├── deployment.yaml  — app + PostgreSQL
└── hpa.yaml         — HorizontalPodAutoscaler (2–10 réplicas)
```
