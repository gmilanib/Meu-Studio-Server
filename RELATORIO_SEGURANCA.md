w# Relatório de avaliação de segurança

**Projeto:** Meu Studio Server  
**Data da avaliação:** 25 de agosto de 2026  
**Última atualização:** 27 de agosto de 2026
**Escopo:** código-fonte, configurações, migrations, dependências Maven, testes e histórico Git local.
**Status pré-deploy:** NÃO APROVADO para exposição pública enquanto os bloqueadores abaixo permanecerem abertos.

## Resumo executivo

As correções de maior urgência relacionadas ao cadastro público, aos papéis de usuário, às credenciais do banco, à fixação de sessão e ao bootstrap do primeiro administrador foram concluídas. Para a fase 1, foi definido um único estúdio piloto com um `USER` autorizado a executar todo o CRUD de clientes; essa regra foi explicitada localmente no Spring Security, mas ainda precisa de testes específicos. CORS e cookies de sessão foram centralizados e parametrizados, e a suíte completa passou com quatro testes. As principais pendências restantes antes do deploy são versionar as alterações e definir os valores reais da infraestrutura de produção.

Status atualizado das correções:

| Estado | Severidade atual | Correção ou pendência |
|---|---|---|
| Concluído | — | Cadastro deixou de ser público e exige papel `ADMIN` |
| Concluído | — | `User.prePersist()` preserva um papel previamente definido |
| Concluído | — | A sessão converte o papel persistido para a autoridade `ROLE_<papel>` |
| Concluído | — | Senha do banco rotacionada e usuário da aplicação sem privilégios administrativos |
| Concluído | — | Credenciais removidas do histórico Git local e remoto |
| Implementado para a fase 1; teste e commit pendentes | — | `ADMIN` e `USER` possuem acesso total aos clientes de um único estúdio piloto |
| Concluído | — | Proteção contra fixação de sessão validada por teste automatizado |
| Concluído | — | Bootstrap controlado do primeiro `ADMIN` validado em banco limpo |
| Pendente | Média | Rate limiting e proteção contra abuso no login |
| Pendente | Média | Política de senha forte |
| Pendente | Média | Atualização do Spring Boot e dependências transitivas |
| Preparado localmente; configuração de PRD pendente | Baixa | CORS centralizado e cookies de sessão parametrizados |
| Pendente | Baixa | Mitigação de enumeração de usuários |
| Pendente | Baixa | Observabilidade e testes automatizados de segurança |

## Checklist pendente antes do deploy

### Bloqueadores

1. Testar a autorização explícita de `ADMIN` e `USER` sobre `/clientes/**` e versionar todas as alterações atuais.
2. Definir `CORS_ALLOWED_ORIGIN`, `SESSION_COOKIE_SECURE` e `SESSION_COOKIE_SAME_SITE` de acordo com os domínios reais e validar em staging com HTTPS.
3. Fornecer credenciais e segredos pelo mecanismo seguro da hospedagem, sem incluí-los na imagem ou no repositório.
4. Definir se a conexão da aplicação com o PostgreSQL utilizará TLS em produção.

### Fortemente recomendados

1. Aplicar rate limiting no login e backoff após falhas.
2. Fortalecer a política de senha.
3. Atualizar o Spring Boot e as dependências transitivas.
4. Criar testes automatizados para autenticação, autorização, CSRF e bootstrap; o teste de troca do ID da sessão já foi implementado.
5. Habilitar auditoria de eventos de segurança sem registrar credenciais ou cookies.
6. Adicionar detecção automática de segredos aos commits e à CI.

## 1. Cadastro e autorização de clientes

**Status: Resolvido para a fase 1; teste e commit da regra explícita pendentes.**

### Evidências

- [`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) agora libera publicamente somente `/auth/csrf` e `/auth/login`; `POST /auth/CreateUser` exige `ROLE_ADMIN`.
- [`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) cria a autoridade a partir do papel persistido, usando o formato `ROLE_<papel>`.
- [`User.java`](src/main/java/com/example/meustudio/auth/User.java) atribui `USER` somente quando o papel está ausente, preservando um `ADMIN` previamente definido.
- A fase 1 terá somente um estúdio piloto e um único `USER`, que deve possuir acesso total aos clientes.
- [`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) possui uma alteração local que restringe `/clientes/**` explicitamente aos papéis `ADMIN` e `USER`.
- [`ClienteService.java`](src/main/java/com/example/meustudio/cliente/ClienteService.java) consulta os clientes globalmente, comportamento compatível com a fase de estúdio único.

### Cenário de exploração

O caminho de exploração por autocadastro foi fechado. O acesso global por `ADMIN` e `USER` é uma decisão consciente para o estúdio piloto, não uma separação multi-tenant.

### Impacto

Uma conta `USER` comprometida terá acesso total aos dados dos clientes. Esse risco é aceito na fase 1 e deverá ser revisto quando houver mais usuários, permissões distintas ou um segundo estúdio.

### Recomendação

1. Criar testes que comprovem acesso total para `ADMIN` e `USER` e bloqueio para anônimos ou papéis inesperados.
2. Versionar a alteração atual de `SecurityConfig.java`.
3. Reabrir esta análise antes de adicionar outro estúdio ou papéis com permissões diferentes.

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

## 3. Fixação de sessão

**Status: Corrigido, validado e versionado no commit `b1e8d6c`.**

### Evidência

[`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) fornece uma `ChangeSessionIdAuthenticationStrategy`. [`SessionService.java`](src/main/java/com/example/meustudio/auth/SessionService.java) executa essa estratégia antes de persistir o novo `SecurityContext`.

### Impacto

[`SessionServiceTest.java`](src/test/java/com/example/meustudio/auth/SessionServiceTest.java) cria uma sessão conhecida antes do login e verifica que seu identificador muda depois da autenticação. O teste foi executado isoladamente com uma execução, zero falhas e zero erros.

### Validação restante

Executar a suíte completa em um ambiente de teste com banco configurado antes do deploy.

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

**Status: Corrigido localmente; valores e validação de PRD pendentes.**

As anotações `@CrossOrigin("*")` foram removidas de [`UserController.java`](src/main/java/com/example/meustudio/auth/UserController.java) e [`ClienteController.java`](src/main/java/com/example/meustudio/cliente/ClienteController.java). [`SecurityConfig.java`](src/main/java/com/example/meustudio/config/SecurityConfig.java) agora concentra a política, recusa origem curinga, aceita somente a origem configurada e limita os headers a `Content-Type`, `Accept` e `X-XSRF-TOKEN`.

[`application.properties`](src/main/resources/application.properties) recebe a origem através de `CORS_ALLOWED_ORIGIN`, mantém `http://localhost:5173` como padrão local e configura o cookie de sessão como `HttpOnly`. `Secure` e `SameSite` são controlados por `SESSION_COOKIE_SECURE` e `SESSION_COOKIE_SAME_SITE`.

[`SecurityConfigTest.java`](src/test/java/com/example/meustudio/config/SecurityConfigTest.java) confirmou que a origem configurada é aceita, uma origem não autorizada é rejeitada e `*` não pode ser usado.

### Validação restante

Definir a origem real do frontend, usar `SESSION_COOKIE_SECURE=true` em HTTPS e escolher `SameSite` conforme a relação entre os domínios. Repetir os testes de preflight e sessão no ambiente de staging.

## 8. Enumeração de usuários

**Severidade: Baixa**

Embora a mensagem de erro seja a mesma, o login de um usuário inexistente termina antes do BCrypt, enquanto o login de um usuário existente executa a comparação cara. A diferença de tempo pode revelar quais nomes estão cadastrados. Tentativas de cadastro duplicado também podem produzir respostas diferentes.

### Recomendação

- Executar uma comparação BCrypt com hash fictício quando o usuário não existir.
- Padronizar respostas de duplicidade e autenticação.
- Aplicar rate limiting e monitorar tentativas de enumeração.

## 9. Observabilidade insuficiente

**Severidade: Baixa**

[`application.properties`](src/main/resources/application.properties) define o Spring Security em `INFO`, permitindo que a aplicação e os testes iniciem sem depender de `LOGLVL`. Para produção, `WARN` continua sendo o nível recomendado para o framework. Não foi encontrada auditoria específica para sucesso/falha de login, criação de contas ou operações destrutivas.

### Recomendação

Registrar eventos de segurança com cuidado para nunca incluir senhas, hashes, cookies, tokens CSRF ou credenciais de banco. Os registros devem permitir detectar força bruta, criação excessiva de contas e exclusões anormais.

## 10. Testes e validação

Existem quatro testes automatizados distribuídos em três classes:

- [`MeuStudioApplicationTests.java`](src/test/java/com/example/meustudio/MeuStudioApplicationTests.java) tenta carregar o contexto completo.
- [`SessionServiceTest.java`](src/test/java/com/example/meustudio/auth/SessionServiceTest.java) verifica que o identificador de uma sessão preexistente muda depois do login.
- [`SecurityConfigTest.java`](src/test/java/com/example/meustudio/config/SecurityConfigTest.java) contém dois testes para origem CORS permitida, origem rejeitada e proibição de curinga.

Em 27 de agosto de 2026, a suíte completa foi executada com o ambiente local carregado: quatro testes passaram, sem falhas ou erros. O contexto conectou ao PostgreSQL, validou quatro migrations e confirmou que o schema já estava atualizado.

Não existem testes automatizados para:

- acesso anônimo e autenticado;
- autorização de `/clientes/**` para `ADMIN`, `USER`, anônimos e papéis inesperados;
- CSRF e integração HTTP completa do CORS;
- política de senha;
- bloqueio e rate limiting;
- bootstrap do primeiro administrador.

A compilação e a suíte completa terminaram com código de saída zero. Ainda são necessários testes de autorização e validação em staging antes da promoção para produção.

## 11. Bootstrap controlado do primeiro ADMIN

**Status: Concluído e validado operacionalmente em 27 de agosto de 2026.**

[`AdminBootstrap.java`](src/main/java/com/example/meustudio/auth/AdminBootstrap.java) é ativado somente quando `app.bootstrap-admin.enabled=true`, valida as configurações obrigatórias, recusa a execução quando já existe um `ADMIN`, codifica a senha e persiste o primeiro administrador. [`application.properties`](src/main/resources/application.properties) mantém o recurso desabilitado por padrão e recebe os valores através de variáveis de ambiente.

A validação em banco limpo confirmou a criação de exatamente um `ADMIN`, o armazenamento da senha com BCrypt e o login com a conta criada. Uma segunda inicialização com o bootstrap ainda habilitado foi recusada, como esperado. Depois da remoção das variáveis `BOOTSTRAP_ADMIN_*`, a aplicação reiniciou normalmente e permaneceu com apenas um administrador.

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

1. Testar a autorização explícita de `ADMIN` e `USER` sobre clientes e versionar as alterações atuais.
2. Definir os domínios, cookies, HTTPS e TLS de produção e validar tudo em staging.
3. Adicionar rate limiting e fortalecer a política de senha.
4. Atualizar o Spring Boot e as dependências transitivas.
5. Criar os testes automatizados de segurança restantes.
6. Adicionar observabilidade e detecção automática de segredos aos commits e à CI.

## Limitações e decisões pendentes

Esta foi principalmente uma análise estática e de dependências. Posteriormente, a API foi iniciada com sucesso contra o PostgreSQL local, mas não foi realizado teste de invasão dinâmico. A configuração externa de produção, rede, proxy e infraestrutura não está no repositório.

As seguintes decisões precisam ser confirmadas antes de definir a solução arquitetural:

1. Qual mecanismo de secrets da hospedagem fornecerá e removerá as variáveis temporárias do bootstrap?
2. A API será publicada atrás de HTTPS e reverse proxy?
3. A conexão entre a aplicação e o PostgreSQL exigirá TLS em produção?
