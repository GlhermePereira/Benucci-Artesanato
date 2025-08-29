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
- Chave segura criada com `Keys.secretKeyFor(SignatureAlgorithm.HS512)`
- Expiração do token configurável via `application.properties`

### Usuário

- Entidade `Usuario` com os campos:
    - `id`, `nome`, `email`, `senha`, `tipo`, `telefone`, `endereco`
- Consulta por email usando JPA/Hibernate

### Segurança

- JWT utilizado na autenticação
- Integração com Spring Security via filtros

### Banco de Dados

- Configuração de datasource Hikari
- EntityManagerFactory inicializado
- Consultas Hibernate funcionando

## Endpoints Implementados

| Endpoint            | Método | Descrição                                    |
|--------------------|--------|----------------------------------------------|
| `/auth/login`       | POST   | Recebe email/senha e gera JWT                |
| `/usuario/{email}`  | GET    | Busca usuário por email (teste)              |

## Testes de JWT

Exemplo de testes unitários com **JUnit 5**:

```java
@Test
void testGenerateToken() {
    String token = jwtUtils.generateToken(usuario);
    assertNotNull(token, "Token não deve ser nulo");
}

@Test
void testGetEmailFromToken() {
    String token = jwtUtils.generateToken(usuario);
    String email = jwtUtils.getEmailFromToken(token);
    assertEquals(usuario.getEmail(), email);
}

@Test
void testValidateToken() {
    String token = jwtUtils.generateToken(usuario);
    assertTrue(jwtUtils.validateToken(token));

    // Token inválido
    String tokenInvalido = token + "123";
    assertFalse(jwtUtils.validateToken(tokenInvalido));
}
```

## Próximos Passos

- Criar testes de integração para os endpoints REST
- Implementar criptografia de senha com **BCrypt**
- Gerenciar roles/tipos de usuário para autorização
- Criar endpoints para cadastro, atualização e remoção de usuários
- Tratar exceções globais para JWT inválido ou expirado
