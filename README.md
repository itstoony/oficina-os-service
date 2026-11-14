# oficina-os-service

Microsserviço de Ordens de Serviço — FIAP Pós-Tech Software Architecture Fase 4.

## Responsabilidade

Gestão completa do ciclo de vida das Ordens de Serviço, Clientes, Veículos, Atendentes e Catálogo de Serviços.

## Stack

- Java 17 + Spring Boot 3.5.3
- PostgreSQL (banco relacional)
- RabbitMQ (mensageria assíncrona)
- Arquitetura Hexagonal (Ports & Adapters) + DDD
- BDD com Cucumber

## Porta

`8080`

## Principais endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Autenticação admin |
| POST/GET | `/api/admin/ordens` | CRUD de Ordens de Serviço |
| POST | `/api/admin/ordens/{id}/enviar-orcamento` | Envia orçamento ao cliente |
| POST | `/api/public/ordens/{numero}/aprovar` | Cliente aprova orçamento |
| POST | `/api/public/ordens/{numero}/recusar` | Cliente recusa orçamento |
| GET | `/api/admin/relatorios/tempo-medio` | Tempo médio de execução |
| POST/GET | `/api/admin/clientes` | CRUD de clientes |
| POST/GET | `/api/admin/veiculos` | CRUD de veículos |
| POST/GET | `/api/admin/servicos` | Catálogo de serviços |

## Eventos publicados

| Routing Key | Quando |
|-------------|--------|
| `os.aberta` | OS criada |
| `os.aprovada` | Cliente aprova orçamento |
| `os.cancelada` | OS cancelada ou recusada |
| `orcamento.solicitado` | Admin envia orçamento |

## Eventos consumidos

| Routing Key | Ação |
|-------------|------|
| `orcamento.gerado` | Atualiza valor total da OS |
| `pagamento.confirmado` | Libera OS para execução |
| `estoque.reservado` | Avança status da OS |
| `estoque.indisponivel` | Cancela OS por falta de estoque |
| `execucao.finalizada` | Marca OS como finalizada |

## Executar localmente

```bash
docker-compose up -d
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw test
```
