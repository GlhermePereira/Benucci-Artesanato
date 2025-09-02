# Documentação da Aplicação BenucciArtesanato

## Estrutura do Projeto

```
src/main/java/
└── br/edu/fatecpg/BenucciArtesanato
    ├── config/          -> Configurações (JWT, segurança)
    ├── controller/      -> Endpoints REST (Auth, Usuário)
    ├── model/           -> Entidades JPA (Usuario)
    ├── repository/      -> Interfaces de acesso ao banco
    └── service/         -> Regras de negócio (AuthService, UserService)
```

## Funcionalidades Implementadas

### Autenticação via JWT

- **Classe `JwtUtils`**
  - `generateToken(Usuario usuario)` → Gera tokens JWT com algoritmo **HS512**
  - `getEmailFromToken(String token)` → Extrai o email do token JWT
  - `validateToken(String token)` → Valida se o token é válido e não expirou
  - `validateToken(String token, Usuario usuario)` → Valida se o token pertence ao usuário e é válido
- **Chave JWT** configurada no `application.properties` via `jwt.secret`
- **Expiração do token** configurável via `application.properties` com `jwt.expirationMs`
- **Segurança:** utiliza algoritmo **HS512** para assinar tokens, garantindo integridade e autenticidade das requisições

### Usuário

- Entidade `Usuario` com os campos:
  - `id`, `nome`, `email`, `senha`, `tipo`, `telefone`, `endereco`
- Consulta por email usando JPA/Hibernate

### Criptografia de Senha

- Senhas armazenadas usando **BCrypt** (hash seguro)
- Garante que a senha não seja salva em texto puro no banco
- Comparação de senha na autenticação é feita usando a função de verificação do BCrypt

## Endpoints Implementados

| Endpoint                | Método | Descrição                             |
|------------------------|--------|---------------------------------------|
| `/auth/login`           | POST   | Recebe email/senha e gera JWT         |
| `/auth/register`        | POST   | Cria um novo usuário e salva no banco |
| `/usuario/{id}`         | GET    | Busca usuário pelo ID e pelo JWT      |
| `/usuario/email/{email}`| GET    | Busca usuário pelo email (teste)      |

### Exemplo de Requisição Login

```json
POST /auth/login
{
  "email": "usuario@teste.com",
  "senha": "senha123"
}
```
### Exemplo Requisição Register

```json 
{
    "id": 1,
    "nome": "Joao",
    "email": "Joao@email.com",
    "senha": "$2a$10$NlLqy.pEuUP8fqccArV..uLZ1M1LLZBZ13i.CQgvGx70Yqf65kkS2",
    "telefone": "13988240253",
    "endereco": "Av. São Paulo, 416 - Boqueirão, Praia Grande - SP, 11701-380",
    "tipo": "cliente"
}
