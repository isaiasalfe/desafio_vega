# Desafio Vega

API REST em Spring Boot para controle de entrada, saída e armazenamento de ingredientes em compartimentos.

## Pré-requisitos

- Java 21+
- Maven Wrapper incluso no projeto (`./mvnw`) — não é necessário instalar o Maven separadamente

## Como rodar localmente

Na raiz do projeto:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em:

```text
http://localhost:8080
```

Na inicialização, o Flyway aplica as migrations e carrega os dados iniciais (tipos de ingrediente e compartimentos C1–C5).

### Verificar se está no ar

```bash
curl http://localhost:8080/api/health
```

Resposta esperada:

```json
{"status":"UP"}
```

### Rodar os testes

```bash
./mvnw test
```

## Banco de dados (H2 em memória)

A aplicação usa H2 em memória. Os dados são perdidos ao encerrar o processo.

| Item | Valor |
|---|---|
| Console | http://localhost:8080/h2-console |
| JDBC URL | `jdbc:h2:mem:desafio_vega` |
| Usuário | `sa` |
| Senha | *(vazia)* |

No console H2, use exatamente a JDBC URL acima ao conectar.

## Principais endpoints

### Ingredientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/ingredientes/entrada` | Registra entrada de ingrediente |
| `POST` | `/ingredientes/saida` | Registra saída de ingrediente |
| `GET` | `/ingredientes` | Lista ingredientes ativos |
| `GET` | `/ingredientes/{id}` | Consulta ingrediente por id |
| `GET` | `/ingredientes/volume` | Volume armazenado por tipo |

Exemplo de entrada:

```bash
curl -X POST http://localhost:8080/ingredientes/entrada \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Acucar",
    "idTipoIngrediente": 1,
    "quantidade": 30,
    "nomeResponsavel": "Maria"
  }'
```

Exemplo de saída:

```bash
curl -X POST http://localhost:8080/ingredientes/saida \
  -H "Content-Type: application/json" \
  -d '{
    "idIngrediente": 1,
    "quantidade": 10,
    "nomeResponsavel": "Joao"
  }'
```

### Compartimentos

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/compartimentos/disponiveis?quantidade={q}&tipo={tipo}` | Compartimentos com espaço para armazenar |
| `GET` | `/compartimentos/disponiveis-para-venda?tipo={tipo}` | Compartimentos com estoque do tipo |

Tipos aceitos: `SECO`, `LIQUIDO`, `REFRIGERADO`.

```bash
curl "http://localhost:8080/compartimentos/disponiveis?quantidade=20&tipo=SECO"
curl "http://localhost:8080/compartimentos/disponiveis-para-venda?tipo=SECO"
```

### Histórico de operações

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/historico?sortBy={data\|compartimento}&order={asc\|desc}` | Lista operações ordenadas |

```bash
curl "http://localhost:8080/historico?sortBy=data&order=desc"
```
