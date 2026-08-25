# Relatório de avaliação de segurança

**Projeto:** Meu Studio Server  
**Data da avaliação:** 25 de agosto de 2026  
**Escopo:** código-fonte, configurações, migrations, dependências Maven, testes e histórico Git local.

## Resumo executivo

O projeto ainda não está seguro para exposição pública. Foram identificadas uma falha crítica, duas falhas altas, riscos médios e lacunas de garantia por ausência de testes de segurança.

Classificação resumida:

| Severidade | Achado |
|---|---|
| Crítica | Cadastro público concede acesso irrestrito aos dados de todos os clientes |
| Alta | Credenciais de banco permanecem recuperáveis no histórico Git |
| Alta | Autenticação manual pode permitir fixação de sessão |
| Média | Cadastro e login permitem esgotamento de CPU por ausência de rate limiting |
| Média | Política de senha aceita senhas extremamente fracas |
| Média | Dependências possuem correções de segurança disponíveis |
| Baixa | Configuração CORS redundante e permissiva nos controllers |
| Baixa | Possibilidade de enumeração de usuários por tempo de resposta |
| Baixa | Observabilidade e testes de segurança insuficientes |

## 1. Cadastro público com acesso total aos clientes

**Severidade: Crítica**

### Evidências

- [`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) libera todas as rotas que correspondem a `/auth/*` e exige apenas autenticação para as demais.
- [`UserController.java`](src/main/java/com/example/meustudio/auth/UserController.java) disponibiliza publicamente `/auth/CreateUser`, `/auth/csrf` e `/auth/login`.
- [`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) concede `ROLE_USER` a todo usuário autenticado.
- [`ClienteController.java`](src/main/java/com/example/meustudio/cliente/ClienteController.java) permite listar, consultar, criar, atualizar e excluir clientes sem verificar papel, proprietário ou estúdio.
- [`ClienteService.java`](src/main/java/com/example/meustudio/cliente/ClienteService.java) consulta os clientes globalmente, sem filtro por usuário ou tenant.

### Cenário de exploração

Uma pessoa externa pode obter um token CSRF, criar uma conta, autenticar-se e acessar ou modificar todos os registros de clientes. CORS e CSRF não substituem autorização e não impedem um cliente HTTP controlado pelo atacante.

### Impacto

Exposição, alteração e exclusão de dados pessoais, incluindo nome, e-mail e telefone.

### Recomendação

1. Definir se a criação de contas será administrativa, por convite ou realmente pública.
2. Associar usuários e clientes a um estúdio ou proprietário, por exemplo, através de `studio_id`.
3. Filtrar todas as consultas e mutações pelo estúdio do usuário autenticado.
4. Definir papéis e permissões, como administrador, funcionário e leitura.
5. Aplicar autorização tanto nos endpoints quanto na camada de serviço.

## 2. Credenciais expostas no histórico Git

**Severidade: Alta; Crítica se as credenciais ainda forem válidas e o repositório tiver sido compartilhado ou publicado.**

### Evidências

Versões antigas de `application.properties` e `application-local.properties` continham URL, usuário e senha do banco como valores literais. Esses dados aparecem em diversas revisões, incluindo `665e76b`, `4f715f4` e `0d4a161`, que continuam alcançáveis pelo histórico associado a `origin/master`.

O estado atual está melhor protegido:

- [`.gitignore`](.gitignore) ignora `.env`.
- O `.env` local possui permissão `600`.
- [`application.properties`](src/main/resources/application.properties) usa `DB_URL`, `DB_USER` e `DB_PASSWORD` como variáveis de ambiente.

Entretanto, remover o segredo do estado atual não o remove dos commits antigos.

### Recomendação

1. Rotacionar imediatamente a senha e, preferencialmente, o usuário do banco.
2. Revogar as credenciais antigas antes de limpar o histórico.
3. Limpar os segredos do histórico Git e atualizar o repositório remoto.
4. Orientar quem já clonou o repositório a substituir seus clones.
5. Adicionar detecção de segredos ao processo de commit e CI.

## 3. Possível fixação de sessão

**Severidade: Alta**

### Evidência

[`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) cria manualmente o `Authentication` e salva o `SecurityContext`, mas não executa uma `SessionAuthenticationStrategy` nem troca explicitamente o identificador da sessão após o login.

### Impacto

Se um atacante conseguir fazer a vítima utilizar uma sessão previamente conhecida, a sessão poderá continuar com o mesmo identificador depois da autenticação, possibilitando sequestro da conta.

### Recomendação

Preferir o fluxo padrão do Spring Security, usando `AuthenticationManager` e os filtros de autenticação. Caso o login permaneça em um controller, executar a estratégia de autenticação de sessão, incluindo a troca do ID, antes de persistir o contexto.

Referência: [Session Management — Spring Security](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/session-management.html).

## 4. Esgotamento de CPU em cadastro e login

**Severidade: Média; pode tornar-se Alta quando a API estiver exposta diretamente à internet.**

### Evidências

- [`Encoder.java`](src/main/java/com/example/meustudio/config/Encoder.java) configura BCrypt com custo 16.
- Cadastro e login são públicos.
- Não foi encontrado rate limiting, backoff, bloqueio temporário ou limite global de concorrência.
- A criação de usuário calcula o hash antes de tentar persistir o registro.

### Impacto

Um atacante pode automatizar solicitações e manter a CPU ocupada com operações BCrypt dispendiosas, degradando ou indisponibilizando a API.

### Recomendação

- Aplicar limites por IP e por nome de usuário.
- Adotar backoff progressivo após falhas.
- Limitar concorrência e tamanho dos corpos HTTP.
- Avaliar convite ou CAPTCHA no cadastro público.
- Medir o custo BCrypt no servidor de produção e escolher um valor que equilibre proteção e disponibilidade.

## 5. Política de senha fraca

**Severidade: Média**

### Evidência

[`CreateUserRequest.java`](src/main/java/com/example/meustudio/auth/CreateUser/CreateUserRequest.java) define somente um máximo de 20 caracteres. Uma senha com um único caractere é aceita.

### Recomendação

- Exigir comprimento mínimo de 12 a 15 caracteres, de acordo com o modelo de autenticação.
- Permitir senhas longas e passphrases.
- Verificar senhas conhecidas como comprometidas.
- Evitar regras artificiais de composição que não produzam entropia real.
- Considerar MFA para operações sensíveis.

## 6. Dependências com correções disponíveis

**Severidade: Média**

[`pom.xml`](pom.xml) utiliza Spring Boot 4.0.6. A árvore efetiva inclui Spring Framework 7.0.7, Spring Security 7.0.5, Spring Data JPA 4.0.5 e Tomcat 11.0.21.

Foram confirmadas as seguintes exposições de versão:

- Spring Security 7.0.5 é afetado pela CVE-2026-41003. O cenário específico, relacionado a SAML `RelyingPartyRegistration`, não aparece no código atual.
- Spring Data JPA 4.0.5 é afetado pela CVE-2026-47834. O cenário exige consulta SQL nativa combinada com `Sort` não confiável, padrão não encontrado no projeto.
- Tomcat 11.0.21 está abaixo de versões que corrigem vulnerabilidades moderadas e baixas. Não foram encontradas no projeto as configurações mais sensíveis, como DIGEST, AJP, WebDAV ou RewriteValve.

### Recomendação

Atualizar o parent Spring Boot para 4.0.8 e executar testes de regressão. A atualização do parent é preferível a sobrescrever individualmente versões transitivas, pois mantém a matriz de dependências testada pelo Spring Boot.

Referências oficiais:

- [Spring Boot 4.0.8](https://spring.io/blog/2026/08/20/spring-boot-4-0-8-available-now/)
- [CVE-2026-41003 — Spring Security](https://spring.io/security/cve-2026-41003/)
- [CVE-2026-47834 — Spring Data JPA](https://spring.io/security/cve-2026-47834/)
- [Vulnerabilidades do Apache Tomcat 11](https://tomcat.apache.org/security-11.html)

## 7. Configuração CORS redundante

**Severidade: Baixa**

[`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) restringe a origem a `http://localhost:5173` e permite credenciais. Porém, `@CrossOrigin("*")` aparece em métodos de [`UserController.java`](src/main/java/com/example/meustudio/auth/UserController.java) e [`ClienteController.java`](src/main/java/com/example/meustudio/cliente/ClienteController.java).

O filtro central atualmente reduz o risco, mas as anotações permissivas criam ambiguidade e podem abrir acesso após uma refatoração ou mudança na ordem dos filtros.

### Recomendação

Manter uma única política CORS central, com origens explícitas carregadas por ambiente, e remover curingas dos controllers.

## 8. Enumeração de usuários

**Severidade: Baixa**

Embora a mensagem de erro seja a mesma, o login de um usuário inexistente termina antes do BCrypt, enquanto o login de um usuário existente executa a comparação cara. A diferença de tempo pode revelar quais nomes estão cadastrados. Tentativas de cadastro duplicado também podem produzir respostas diferentes.

### Recomendação

- Executar uma comparação BCrypt com hash fictício quando o usuário não existir.
- Padronizar respostas de duplicidade e autenticação.
- Aplicar rate limiting e monitorar tentativas de enumeração.

## 9. Observabilidade insuficiente

**Severidade: Baixa**

[`application.properties`](src/main/resources/application.properties) define `logging.level.org.springframework.security=off`. Não foi encontrada auditoria específica para sucesso/falha de login, criação de contas ou operações destrutivas.

### Recomendação

Registrar eventos de segurança com cuidado para nunca incluir senhas, hashes, cookies, tokens CSRF ou credenciais de banco. Os registros devem permitir detectar força bruta, criação excessiva de contas e exclusões anormais.

## 10. Testes e validação

O único teste existente, [`MeuStudioApplicationTests.java`](src/test/java/com/example/meustudio/MeuStudioApplicationTests.java), apenas tenta carregar o contexto. Durante a avaliação, ele falhou porque `DB_URL` não estava disponível no ambiente de teste.

Não existem testes automatizados para:

- acesso anônimo e autenticado;
- autorização por papel ou proprietário;
- CSRF e CORS;
- troca do identificador da sessão no login;
- política de senha;
- bloqueio e rate limiting;
- proteção contra acesso entre usuários ou estúdios.

A compilação das classes estava atualizada, mas a suíte terminou com um erro de infraestrutura antes de validar o comportamento da aplicação.

## Pontos positivos

- Senhas são armazenadas com BCrypt, não em texto puro no código atual.
- CSRF está habilitado.
- O CORS central usa uma origem explícita para requisições com credenciais.
- As consultas atuais usam métodos derivados do Spring Data, sem SQL manual vulnerável a injeção.
- DTOs evitam vincular diretamente entidades inteiras ao corpo das requisições.
- `spring.jpa.show-sql` está desativado.
- `spring.jpa.open-in-view` está desativado.
- A senha do usuário não é devolvida pelos DTOs de resposta.

## Ordem sugerida de correção

1. Rotacionar as credenciais do banco expostas no Git.
2. Fechar ou controlar o cadastro público.
3. Implementar autorização por estúdio/proprietário e papéis.
4. Corrigir o fluxo de autenticação e a rotação de sessão.
5. Adicionar rate limiting e fortalecer a política de senha.
6. Atualizar o Spring Boot e as dependências transitivas.
7. Consolidar o CORS e configurar cookies/TLS de produção.
8. Criar testes automatizados de segurança e observabilidade.

## Limitações e decisões pendentes

Esta foi uma análise estática e de dependências. A API não foi iniciada contra o banco real e não foi realizado teste de invasão dinâmico. A configuração externa de produção, rede, proxy, banco e infraestrutura não estava no repositório.

As seguintes decisões precisam ser confirmadas antes de definir a solução arquitetural:

1. O cadastro de usuários deve ser público, administrativo ou por convite?
2. Cada usuário deve visualizar todos os clientes ou somente clientes do seu estúdio?
3. Existem papéis distintos, como administrador, recepcionista e profissional?
4. A API será publicada atrás de HTTPS e reverse proxy?
5. As credenciais encontradas no histórico já foram rotacionadas?
6. A conexão entre a aplicação e o PostgreSQL exige TLS e usa um usuário com privilégios mínimos?

