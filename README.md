# Customer Management API

## 1. Visão Geral

Este projeto consiste em um microsserviço para gerenciamento de clientes, desenvolvido como parte de um desafio técnico para a posição de Desenvolvedor Java Pleno.

A API permite realizar operações de CRUD para clientes, além de consultar um serviço externo para obter o score de crédito de cada cliente. A aplicação foi construída com foco em boas práticas de desenvolvimento, padrões de design, segurança e resiliência.

## 2. Features

- **Gerenciamento Completo de Clientes**: API REST para operações de CRUD.
- **Consultas Avançadas**: Filtro de clientes por status e busca por nome.
- **Integração com Serviço Externo**: Consulta de score de crédito de forma resiliente, simulada com WireMock.
- **Segurança Robusta**: Autenticação via Basic Auth e autorização baseada em papéis (`USER` e `ADMIN`).
- **Tratamento de Erros Abrangente**: Respostas HTTP consistentes para erros de negócio, validação e falhas de sistema.
- **Configuração Externalizada**: Flexibilidade para alterar configurações da aplicação sem a necessidade de recompilar o código.
- **Observabilidade**: Logs detalhados em todas as camadas da aplicação para facilitar o rastreamento e diagnóstico.

## 3. Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Data JPA**
- **Spring Security**
- **Spring Web**
- **Spring Boot Admin Client** (monitoramento)
- **Spring Validation**
- **Spring Rest Client**
- **Maven** como gerenciador de dependências
- **H2 Database** (desenvolvimento e testes)
- **Lombok** para redução de código boilerplate
- **WireMock** para simulação do serviço externo de score
- **JUnit 5 & Mockito** para testes unitários

## 4. Pré-requisitos

Para executar o projeto localmente, você precisará ter instalado:

- **JDK 21**
- **Apache Maven** 3.8 ou superior
- **Docker** e **Docker Compose** (para executar o serviço de score simulado)

## 5. Como Executar o Projeto

Siga os passos abaixo para ter a aplicação e seus serviços de apoio rodando localmente.

### 5.1. Clone o Repositório

```bash
git clone https://github.com/mrpine19/customer-management-api
cd customer-management-api
```

### 5.2. Execute o Serviço de Score com WireMock

O serviço externo de score é simulado usando WireMock e executado em um contêiner Docker. Este passo é **essencial** para testar os endpoints de consulta de score.

No terminal, a partir da raiz do projeto, execute o seguinte comando:

**Para Linux, macOS ou Git Bash no Windows:**

```bash
docker run --rm -d --name wiremock-score -p 8081:8080 -v "$(pwd)/wiremock:/home/wiremock" wiremock/wiremock:3.9.1
```

**Para PowerShell no Windows:**

```powershell
docker run --rm -d --name wiremock-score -p 8081:8080 -v "${PWD}\wiremock:/home/wiremock" wiremock/wiremock:3.9.1
```

Este comando irá:
- Iniciar um contêiner Docker chamado `wiremock-score`.
- Mapear a porta `8081` do seu host para a porta `8080` do contêiner.
- Montar o diretório local `./wiremock` (que contém os cenários de teste) dentro do contêiner.

### 5.3. Execute a Aplicação Principal

Com o serviço de score rodando, você pode iniciar a API principal.

Em um novo terminal, a partir da raiz do projeto, execute o comando Maven:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## 6. Como Executar os Testes

### 6.1. Executar Todos os Testes

Para rodar a suíte completa de testes unitários e de integração:

```bash
mvn clean install
```

ou simplesmente:

```bash
mvn test
```

## 7. Estrutura do Projeto

```
src/main/java/com/br/customer/
├── config/                    # Configurações da aplicação
│   ├── SecurityConfig.java    # Configuração de segurança e autenticação
│   └── RestClientConfig.java  # Configuração do cliente HTTP
├── controller/                # Controllers REST
│   └── CustomerController.java
├── service/                   # Lógica de negócio
│   ├── CustomerService.java
│   └── ScoreService.java
├── repository/                # Acesso a dados (Spring Data JPA)
│   └── CustomerRepository.java
├── model/                     # Entidades JPA
│   └── Customer.java
├── dto/                       # Data Transfer Objects
│   ├── CustomerRequest.java
│   ├── CustomerResponse.java
│   └── ScoreResponse.java
├── exception/                 # Exceções customizadas
│   ├── ResourceNotFoundException.java
│   ├── DuplicateCpfException.java
│   ├── ScoreServiceException.java
│   └── GlobalExceptionHandler.java (ControllerAdvice)
└── CustomerManagementApiApplication.java

src/main/resources/
├── application.properties     # Configurações padrão
└── application-dev.properties # Configurações de desenvolvimento

src/test/java/com/br/customer/
├── controller/
├── service/
└── repository/
```

## 8. Padrões e Decisões Arquiteturais

### 8.1. Layered Architecture (Arquitetura em Camadas)

A aplicação segue o padrão de arquitetura em camadas:

```
┌─────────────────────────────────────────────────┐
│          Controllers (HTTP/REST)                │
├─────────────────────────────────────────────────┤
│          Services (Lógica de Negócio)           │
├─────────────────────────────────────────────────┤
│          Repositories (Acesso a Dados)          │
├─────────────────────────────────────────────────┤
│          Database (H2)                          │
└─────────────────────────────────────────────────┘
```

**Responsabilidades de cada camada:**

- **Controller**: Recebe requisições HTTP, valida entrada básica e delega para o serviço
- **Service**: Contém a lógica de negócio, orquestra chamadas a repositórios e serviços externos
- **Repository**: Abstrai o acesso aos dados usando Spring Data JPA
- **Model/Entity**: Representa as entidades do domínio mapeadas para o banco de dados

### 8.2. Tratamento de Exceções Centralizado

Utiliza `@ControllerAdvice` com `@ExceptionHandler` para tratamento centralizado de erros, garantindo:

- Respostas consistentes em formato JSON
- Códigos HTTP apropriados
- Mensagens de erro informativas
- Stack traces apenas em ambiente de desenvolvimento

**Exemplo de resposta de erro:**

```json
{
  "timestamp": "2024-08-17T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "CPF já cadastrado no sistema",
  "path": "/customers"
}
```

### 8.3. Segurança

- **Autenticação**: Basic Authentication com usuários configuráveis
- **Autorização**: Role-Based Access Control (RBAC) com papéis `ROLE_USER` e `ROLE_ADMIN`
- **Validação**: Bean Validation com `@Valid` em entidades DTO
- **Configuração Externa**: Credenciais externalizáveis via variáveis de ambiente

### 8.4. Integração com Serviço Externo

- **Resiliência**: Timeout configurável para evitar travamentos
- **Simulação em Desenvolvimento**: WireMock para mockar respostas do serviço de score
- **Tratamento de Falhas**: Exceções específicas e tratamento adequado de cenários de erro

## 9. Utilização da API

### 9.1. Autenticação e Autorização

A API utiliza **Basic Authentication**. As credenciais padrão estão configuradas no `application.properties` e podem ser externalizadas.

**Papéis e Permissões:**

- **`ADMIN`**: Acesso total a todos os endpoints (CRUD completo)
    - Usuário: `admin`
    - Senha: `admin123`
- **`USER`**: Acesso somente leitura (endpoints `GET`)
    - Usuário: `user`
    - Senha: `user123`

### 9.2. Testando os Endpoints

A maneira mais fácil de testar todos os cenários da API é utilizando o arquivo `api.http` na raiz do projeto. Se você utiliza uma IDE como IntelliJ IDEA (com o plugin de HTTP Client) ou VS Code (com a extensão REST Client), basta abrir o arquivo e executar as requisições.

O arquivo `api.http` já está configurado com todas as chamadas, variáveis e cenários de teste, incluindo:

- Operações de CRUD com o usuário `ADMIN`
- Testes de validação (CPF duplicado, dados inválidos)
- Testes de permissão (o que o `USER` pode e não pode fazer)
- Cenários de integração com o serviço de score (sucesso, erro, timeout, etc.)
- Testes de acesso não autenticado

### 9.3. Tabela de Endpoints

| Método   | Endpoint                  | Papel Mínimo | Descrição                                            |
|----------|---------------------------|--------------|------------------------------------------------------|
| `GET`    | `/customers`              | `USER`       | Lista todos os clientes ou filtra por `status`.      |
| `GET`    | `/customers/{id}`         | `USER`       | Consulta um cliente específico pelo seu ID.            |
| `GET`    | `/customers/search`       | `USER`       | Busca clientes cujo nome contenha o valor do parâmetro `name`. |
| `GET`    | `/customers/{id}/score`   | `USER`       | Consulta o score de crédito de um cliente.             |
| `POST`   | `/customers`              | `ADMIN`      | Cadastra um novo cliente.                            |
| `PUT`    | `/customers/{id}`         | `ADMIN`      | Altera os dados de um cliente existente.             |
| `DELETE` | `/customers/{id}`         | `ADMIN`      | Exclui um cliente existente.                         |

### 9.4. Exemplo de Requisições

**Criar um novo cliente (requer ADMIN):**

```bash
curl -u admin:admin123 -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpf": "12345678901",
    "email": "joao@example.com",
    "status": "ACTIVE"
  }'
```

**Listar todos os clientes:**

```bash
curl -u user:user123 http://localhost:8080/customers
```

**Consultar score de um cliente:**

```bash
curl -u user:user123 http://localhost:8080/customers/1/score
```

## 10. Tratamento de Erros

### 10.1. Códigos HTTP Retornados

| Código | Situação |
|--------|----------|
| 200 | Requisição bem-sucedida (GET, PUT, DELETE) |
| 201 | Recurso criado com sucesso (POST) |
| 400 | Requisição inválida (validação falhou, ex: CPF duplicado) |
| 401 | Sem autenticação (credenciais inválidas ou não fornecidas) |
| 403 | Sem autorização (usuário não possui permissão) |
| 404 | Recurso não encontrado |
| 500 | Erro interno do servidor |
| 503 | Serviço indisponível (ex: score service offline) |

### 10.2. Erros de Negócio Principais

| Erro | Código HTTP | Mensagem |
|------|------------|----------|
| CPF duplicado | 400 | `"CPF já cadastrado no sistema"` |
| Cliente não encontrado | 404 | `"Cliente com ID {id} não encontrado"` |
| Validação de CPF inválida | 400 | `"CPF inválido"` |
| Score service offline | 503 | `"Serviço de score indisponível"` |
| Timeout ao chamar score | 504 | `"Timeout ao consultar score de crédito"` |

### 10.3. Exemplo de Resposta de Erro

```json
{
  "timestamp": "2024-08-17T10:30:15Z",
  "status": 400,
  "error": "Bad Request",
  "message": "CPF já cadastrado no sistema",
  "path": "/customers"
}
```

## 11. Integração com Serviço de Score

A aplicação consome um serviço externo para consultar o score de crédito de cada cliente.

### 11.1. Resiliência

- **Timeout Configurável**: Padrão de 5 segundos para leitura, 3 segundos para conexão
- **Simulação com WireMock**: Em desenvolvimento, o serviço é mockado para facilitar testes
- **Tratamento de Falhas**: Exceções específicas para diferentes cenários (timeout, servidor offline, dados inválidos)

### 11.2. Cenários de Teste Disponíveis no WireMock

O diretório `wiremock/mappings/` contém os seguintes cenários:

- `score-success.json`: Retorna score válido (200)
- `score-not-found.json`: CPF não encontrado (404)
- `score-server-error.json`: Erro interno do servidor (500)
- `score-timeout.json`: Simula timeout na resposta
- `score-invalid-body.json`: Resposta com corpo inválido

### 11.3. Exemplo de Resposta do Score

```json
{
  "customerId": 1,
  "score": 750,
  "grade": "A",
  "lastUpdate": "2024-08-17T10:30:00Z"
}
```

## 12. Configurações Externalizadas

As seguintes propriedades podem ser configuradas via variáveis de ambiente para sobrescrever os valores padrão do `application.properties`:

| Propriedade                     | Variável de Ambiente              | Padrão                               | Descrição                                         |
|---------------------------------|-----------------------------------|--------------------------------------|---------------------------------------------------|
| `score.api.base-url`            | `SCORE_API_BASE_URL`              | `http://localhost:8081`              | URL base do serviço externo de score.             |
| `score.api.connect-timeout-ms`  | `SCORE_API_CONNECT_TIMEOUT_MS`    | `3000`                               | Timeout de conexão com o serviço de score (em ms). |
| `score.api.read-timeout-ms`     | `SCORE_API_READ_TIMEOUT_MS`       | `5000`                               | Timeout de leitura do serviço de score (em ms).    |
| `app.security.user.username`    | `APP_SECURITY_USER_USERNAME`      | `user`                               | Nome de usuário para o papel `USER`.              |
| `app.security.user.password`    | `APP_SECURITY_USER_PASSWORD`      | `user123`                            | Senha para o papel `USER`.                        |
| `app.security.admin.username`   | `APP_SECURITY_ADMIN_USERNAME`     | `admin`                              | Nome de usuário para o papel `ADMIN`.             |
| `app.security.admin.password`   | `APP_SECURITY_ADMIN_PASSWORD`     | `admin123`                           | Senha para o papel `ADMIN`.                       |

## 13. Autor

**Gustavo Pinheiro de Oliveira**

- Email: [mrpine19@gmail.com](mailto:mrpine19@gmail.com)
- GitHub: [@mrpine19](https://github.com/mrpine19)
- LinkedIn: www.linkedin.com/in/gustavo-pinheiro-de-oliveira-0165281b5
