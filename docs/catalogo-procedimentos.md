# Catálogo de procedimentos — implementação e validação

## Regras acordadas

Área de catálogo, sem registro detalhado de atendimentos. Perfis USER e ADMIN gerenciam procedimentos. Cadastro inicia ativo; edição preserva situação; desativação é reversível e não apaga faturamentos. Nome, preço positivo e duração inteira positiva são obrigatórios. Descrição e categoria são opcionais. Categoria é texto com sugestões, sem tabela própria. Nomes são únicos inclusive entre inativos, ignorando caixa e espaços nas extremidades.

## Integração e decisões

- V8 cria `procedimentos` e adiciona `faturamentos.procedimento_id`, nulo para o histórico anterior. Não faz importação automática de texto antigo.
- `POST /financeiro/lancar` exige `procedimentoId`; o servidor valida existência e situação, guarda o ID e copia o nome. Mantém `valor` enviado, permitindo preço diferente do catálogo.
- Consultas e respostas financeiras continuam expondo `procedimento` como nome histórico. Renomear, reajustar ou desativar não modifica registros anteriores.
- Um bloqueio de linha serializa edição/desativação e lançamento na transação. Um índice único em `lower(trim(nome))` evita duplicatas inclusive em requisições simultâneas; validação prévia fornece mensagem amigável.
- Duração fracionária é rejeitada na desserialização. As demais restrições são aplicadas por Bean Validation e reforçadas no banco.
- Lista: 20 por página, máximo 50, ordenada por nome sem distinguir caixa e ID. Filtros por trecho literal de nome, categoria exata e situação; API sem situação inclui todos, UI inicia com ativos.
- Frontend possui busca paginada no seletor financeiro. Seleção sugere preço, troca aplica o novo preço e edição manual determina o valor cobrado. Erros 400/404 no lançamento limpam a seleção e recarregam o catálogo.

## Testes e resultados esperados

`ProcedimentoApiTest` usa API real com MockMvc, filtros de segurança, transações e PostgreSQL. Espera sucesso em cadastro/edição/reativação, filtros e paginação; 400 para dados inválidos/duplicatas/inativos; 404 para IDs inexistentes; 403 para sessão ou CSRF ausentes. Verifica nome e valor financeiros preservados após editar/desativar. Cada caso reverte sua transação.

`ProcedimentoMigrationTest` cria um schema aleatório exclusivo, aplica V1–V7, insere faturamento histórico e aplica V8. Espera texto e valor intactos, vínculo nulo, rejeição de nome duplicado pelo índice e de preço zero pelo CHECK. Remove somente seu schema ao terminar.

Os testes antigos continuam na suíte. O teste de contexto usa o perfil `test`; esse perfil não usa PGHOST/PGDATABASE da aplicação. Configure `TEST_DATABASE_URL`, `TEST_DATABASE_USER` e `TEST_DATABASE_PASSWORD` para um banco **exclusivo de testes**. Padrão local: `jdbc:postgresql://127.0.0.1:55439/meu_studio_procedimentos_test`, usuário `gmb`, senha vazia. Não aponte essas variáveis para produção.

Comando no diretório do backend:

```bash
./mvnw test -q
```

O Maven Wrapper compila e executa toda a suíte Java, com saída resumida. Requer o banco de testes em execução. Os relatórios ficam em `target/surefire-reports`.

## Entrega

Implementação local; sem publicação em produção. Coordenar frontend e backend devido à mudança do contrato de lançamento. Criar pelo menos um procedimento ativo antes de usar o novo financeiro. A API está documentada em `API.md` e na cópia do frontend.

## Verificação nesta sessão

- 37 testes Java aprovados, incluindo API e migração, com PostgreSQL 18 isolado em `/tmp`, porta 55439; nenhuma falha ou teste ignorado.
- 13 testes de interface, lint e build aprovados no frontend. Ferramentas de teste instaladas e lockfile atualizado.
- Inspeção visual não realizada: a ferramenta de navegação retornou que não há navegador disponível na sessão. O frontend e backend locais de QA foram iniciados e encerrados; seus dados ficaram apenas no banco temporário.
- Nenhum banco de produção acessado; nenhuma migração antiga alterada.
