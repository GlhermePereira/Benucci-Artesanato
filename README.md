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
    └── service/         -> Regras de negócio (AuthService, UserService)
```

## Funcionalidades Implementadas

### Autenticação via JWT

1. [x] **Classe `JwtUtils`**
  - `generateToken(Usuario usuario)` → Gera tokens JWT com algoritmo **HS512**
  - `getEmailFromToken(String token)` → Extrai o email do token JWT
  - `validateToken(String token)` → Valida se o token é válido e não expirou
  - `validateToken(String token, Usuario usuario)` → Valida se o token pertence ao usuário e é válido
2. [x] **Chave JWT** configurada no `application.properties` via `jwt.secret`
3. [x] **Expiração do token** configurável via `application.properties` com `jwt.expirationMs`
4. [x] **Segurança:** utiliza algoritmo **HS*2
### Usuário

- Entidade `Usuario` com os campos:
  - `id`, `name`, `email`, `password`, `type`, `phoneNumber`, `address`
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
| `/users/{id}`         | GET    | Busca usuário pelo ID e pelo JWT      |
| `/users/email/{email}`| GET    | Busca usuário pelo email (teste)      |

### Exemplo de Requisição Login

```jsonPOST /auth/login
{
  "email": "Joao@email.com",
  "password": "Joao"
}

```
### Exemplo Requisição Register

```json 
{
  "name": "Joao",
  "email": "Joao@email.com",
  "password": "Joao",
  "phoneNumber": "133456789",
  "address": "Rua X232, 123"
}
```


### Rotas da API

### Autenticação
`POST /auth/register` → Cadastrar novo usuário

`POST /auth/login` → Login e geração de token JWT

### Usuários

`GET /usuarios/{id}` → Retorna dados do usuário

`PUT /usuarios/{id}` → Atualiza usuário

`DELETE /usuarios/{id}` → Remove usuário

### Produtos

`GET /produtos` → Lista todos os produtos (filtros opcionais por categoria ou mais vendidos)

`GET /produtos/{id}` → Retorna produto específico

`POST /produtos` → Adiciona novo produto (admin)

`PUT /produtos/{id}` → Atualiza produto (admin)

`DELETE /produtos/{id}` → Remove produto (admin)

### Pedidos
`POST /pedidos` → Cria novo pedido

`GET /pedidos/{id}` → Detalhes de um pedido

`GET /pedidos/usuario/{usuarioId}` → Histórico de pedidos do usuário

`PUT /pedidos/{id}/status` → Atualiza status do pedido (admin)

### Categorias

`GET /categorias` → Lista categorias

`POST /categorias` → Cria nova categoria (admin)

### Recomendação (Machine Learning)

`GET /recomendacoes/{usuarioId}` → Recomendações personalizadas

`GET /recomendacoes/populares` → Produtos mais vendidos em determinada época
