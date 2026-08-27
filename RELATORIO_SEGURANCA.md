# Relatório de avaliação de segurança

**Projeto:** Meu Studio Server  
**Data da avaliação:** 25 de agosto de 2026  
**Última atualização:** 26 de agosto de 2026
**Escopo:** código-fonte, configurações, migrations, dependências Maven, testes e histórico Git local.

## Resumo executivo

As correções de maior urgência relacionadas ao cadastro público, aos papéis de usuário e às credenciais do banco foram concluídas. O bootstrap controlado do primeiro administrador foi implementado e compilado, mas ainda precisa ser validado em um banco sem `ADMIN`. O projeto permanece com pendências relevantes antes do deploy, principalmente autorização sobre clientes, fixação de sessão e proteção contra abuso dos endpoints públicos.

Status atualizado das correções:

| Estado | Severidade atual | Correção ou pendência |
|---|---|---|
| Concluído | — | Cadastro deixou de ser público e exige papel `ADMIN` |
| Concluído | — | `User.prePersist()` preserva um papel previamente definido |
| Concluído | — | A sessão converte o papel persistido para a autoridade `ROLE_<papel>` |
| Concluído | — | Senha do banco rotacionada e usuário da aplicação sem privilégios administrativos |
| Concluído | — | Credenciais removidas do histórico Git local e remoto |
| Pendente | Alta | Autorização por papel, proprietário ou estúdio nos endpoints de clientes |
| Pendente | Alta | Proteção contra fixação de sessão no login manual |
| Implementado; validação pendente | Alta operacional | Bootstrap controlado do primeiro `ADMIN` em produção |
| Pendente | Média | Rate limiting e proteção contra abuso no login |
| Pendente | Média | Política de senha forte |
| Pendente | Média | Atualização do Spring Boot e dependências transitivas |
| Pendente | Baixa | Consolidação da configuração CORS |
| Pendente | Baixa | Mitigação de enumeração de usuários |
| Pendente | Baixa | Observabilidade e testes automatizados de segurança |

## Checklist pendente antes do deploy

### Bloqueadores

1. Validar o bootstrap em um banco sem `ADMIN`, confirmar o login criado, remover todas as variáveis `BOOTSTRAP_ADMIN_*` e reiniciar a aplicação com o bootstrap desabilitado.
2. Corrigir a fixação de sessão e comprovar que o identificador da sessão muda após um login bem-sucedido.
3. Definir se usuários `USER` podem listar, alterar e excluir todos os clientes; aplicar autorização caso não possam.
4. Configurar HTTPS, cookies de sessão seguros e a origem CORS real do frontend de produção.
5. Fornecer credenciais e segredos pelo mecanismo seguro da hospedagem, sem incluí-los na imagem ou no repositório.

### Fortemente recomendados

1. Aplicar rate limiting no login e backoff após falhas.
2. Fortalecer a política de senha.
3. Atualizar o Spring Boot e as dependências transitivas.
4. Criar testes automatizados para autenticação, autorização, CSRF, sessão e bootstrap.
5. Habilitar auditoria de eventos de segurança sem registrar credenciais ou cookies.
6. Adicionar detecção automática de segredos aos commits e à CI.

## 1. Cadastro público corrigido; autorização de clientes pendente

**Status: Parcialmente concluído**
**Severidade restante: Alta se usuários comuns não puderem administrar todos os clientes.**

### Evidências

- [`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) agora libera publicamente somente `/auth/csrf` e `/auth/login`; `POST /auth/CreateUser` exige `ROLE_ADMIN`.
- [`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) cria a autoridade a partir do papel persistido, usando o formato `ROLE_<papel>`.
- [`User.java`](src/main/java/com/example/meustudio/auth/User.java) atribui `USER` somente quando o papel está ausente, preservando um `ADMIN` previamente definido.
- [`ClienteController.java`](src/main/java/com/example/meustudio/cliente/ClienteController.java) permite listar, consultar, criar, atualizar e excluir clientes sem verificar papel, proprietário ou estúdio.
- [`ClienteService.java`](src/main/java/com/example/meustudio/cliente/ClienteService.java) consulta os clientes globalmente, sem filtro por usuário ou tenant.

### Cenário de exploração

O caminho de exploração por autocadastro foi fechado. Entretanto, qualquer conta autenticada criada pelo administrador ainda pode acessar ou modificar globalmente os registros de clientes. Se todas as contas pertencerem a um único estúdio e forem igualmente confiáveis, isso pode ser uma decisão de negócio; caso contrário, falta controle de acesso.

### Impacto

Exposição, alteração e exclusão de dados pessoais, incluindo nome, e-mail e telefone.

### Recomendação

1. Confirmar quais ações `ADMIN` e `USER` podem executar sobre clientes.
2. Se houver mais de um estúdio, associar usuários e clientes através de `studio_id`.
3. Filtrar consultas e mutações pelo estúdio ou proprietário autenticado.
4. Aplicar autorização tanto nos endpoints quanto na camada de serviço.

## 2. Credenciais expostas no histórico Git

**Status: Resolvido em 26 de agosto de 2026.**

### Evidências

Versões antigas de `application.properties` e `application-local.properties` continham URL, usuário e senha do banco como valores literais.

As seguintes correções foram concluídas:

- A senha exposta foi rotacionada e validada.
- O usuário PostgreSQL da aplicação deixou de possuir privilégios administrativos.
- [`.gitignore`](.gitignore) ignora `.env`.
- O `.env` local possui permissão `600`.
- [`application.properties`](src/main/resources/application.properties) usa `DB_URL`, `DB_USER` e `DB_PASSWORD` como variáveis de ambiente.
- O histórico foi reescrito e publicado novamente no GitHub.
- Um clone novo do remoto confirmou zero atribuições literais de credenciais e ausência de `.env` e `application-local.properties` no histórico.
- A cópia local foi alinhada ao histórico limpo e os objetos antigos foram removidos.

### Prevenção restante

Adicionar detecção automática de segredos ao processo de commit e à CI.

## 3. Possível fixação de sessão

**Severidade: Alta**

### Evidência

[`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) cria manualmente o `Authentication` e salva o `SecurityContext`, mas não executa uma `SessionAuthenticationStrategy` nem troca explicitamente o identificador da sessão após o login.

### Impacto

Se um atacante conseguir fazer a vítima utilizar uma sessão previamente conhecida, a sessão poderá continuar com o mesmo identificador depois da autenticação, possibilitando sequestro da conta.

### Recomendação

Preferir o fluxo padrão do Spring Security, usando `AuthenticationManager` e os filtros de autenticação. Caso o login permaneça em um controller, executar a estratégia de autenticação de sessão, incluindo a troca do ID, antes de persistir o contexto.

Referência: [Session Management — Spring Security](https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/session-management.html).

## 4. Esgotamento de CPU no login

**Severidade: Média; pode tornar-se Alta quando a API estiver exposta diretamente à internet.**

### Evidências

- [`Encoder.java`](src/main/java/com/example/meustudio/config/Encoder.java) configura BCrypt com custo 16.
- O login permanece público, como necessário para autenticação; o cadastro agora exige `ADMIN`.
- Não foi encontrado rate limiting, backoff, bloqueio temporário ou limite global de concorrência.
- A criação de usuário calcula o hash antes de tentar persistir o registro.

### Impacto

Um atacante pode automatizar solicitações e manter a CPU ocupada com operações BCrypt dispendiosas, degradando ou indisponibilizando a API.

### Recomendação

- Aplicar limites por IP e por nome de usuário.
- Adotar backoff progressivo após falhas.
- Limitar concorrência e tamanho dos corpos HTTP.
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

[`application.properties`](src/main/resources/application.properties) permite configurar o nível de log do Spring Security através de `LOGLVL`. Não foi encontrada auditoria específica para sucesso/falha de login, criação de contas ou operações destrutivas.

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

## 11. Bootstrap controlado do primeiro ADMIN

**Status: Implementado; validação operacional pendente.**

[`AdminBootstrap.java`](src/main/java/com/example/meustudio/auth/AdminBootstrap.java) é ativado somente quando `app.bootstrap-admin.enabled=true`, valida as configurações obrigatórias, recusa a execução quando já existe um `ADMIN`, codifica a senha e persiste o primeiro administrador. [`application.properties`](src/main/resources/application.properties) mantém o recurso desabilitado por padrão e recebe os valores através de variáveis de ambiente.

A compilação passou após a implementação. Para concluir a pendência, ainda é necessário executar o fluxo completo em um banco sem administrador, testar o login, remover as variáveis temporárias e confirmar que a aplicação reinicia com o bootstrap desabilitado.

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

1. Corrigir o fluxo de autenticação e a rotação do identificador de sessão.
2. Definir e implementar autorização sobre clientes por papel, proprietário ou estúdio.
3. Validar operacionalmente o bootstrap do primeiro `ADMIN` em um banco limpo.
4. Adicionar rate limiting e fortalecer a política de senha.
5. Atualizar o Spring Boot e as dependências transitivas.
6. Consolidar o CORS e configurar cookies/TLS de produção.
7. Criar testes automatizados de segurança e observabilidade.
8. Adicionar detecção automática de segredos aos commits e à CI.

## Limitações e decisões pendentes

Esta foi principalmente uma análise estática e de dependências. Posteriormente, a API foi iniciada com sucesso contra o PostgreSQL local, mas não foi realizado teste de invasão dinâmico. A configuração externa de produção, rede, proxy e infraestrutura não está no repositório.

As seguintes decisões precisam ser confirmadas antes de definir a solução arquitetural:

1. Cada usuário deve visualizar todos os clientes ou somente clientes do seu estúdio?
2. Quais operações sobre clientes devem ser exclusivas do `ADMIN`?
3. Qual mecanismo de secrets da hospedagem fornecerá e removerá as variáveis temporárias do bootstrap?
4. A API será publicada atrás de HTTPS e reverse proxy?
5. A conexão entre a aplicação e o PostgreSQL exigirá TLS em produção?
