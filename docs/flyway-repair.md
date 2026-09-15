# Modo de manutenção do Flyway

## Finalidade

O backend possui um modo explícito para executar `Flyway.repair()` antes da inicialização do Spring. Ele existe para recuperar o histórico do Flyway quando uma migration aplicada apresenta divergência de checksum e o backend não permanece ativo por tempo suficiente para acesso SSH.

O reparo altera apenas a tabela de histórico do Flyway. Ele não transforma a estrutura das tabelas de negócio para corresponder aos arquivos atuais. Antes de ativá-lo, compare o schema real com as migrations e, sempre que o plano da Railway permitir, crie um backup.

## Proteções

- O modo normal permanece ativo quando `FLYWAY_REPAIR` está ausente ou tem valor diferente de `true`.
- O reparo exige simultaneamente `FLYWAY_REPAIR_CONFIRM=repair-schema-history`.
- `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` e `PGPASSWORD` devem estar preenchidas.
- A senha não é registrada pela implementação.
- Depois do reparo, o processo encerra sem iniciar o servidor HTTP. Isso diferencia claramente o deploy de manutenção de um deploy normal.

## Procedimento na Railway

1. Confirme que o schema existente corresponde às migrations disponíveis no artefato que será implantado.
2. No serviço backend, restaure o Start Command normal ou deixe-o vazio para a autodetecção da Railway.
3. Adicione as variáveis `FLYWAY_REPAIR=true` e `FLYWAY_REPAIR_CONFIRM=repair-schema-history`.
4. Implante a versão que contém este modo de manutenção.
5. Confirme nos logs a mensagem `Flyway repair completed; application startup intentionally skipped.` e a ação de alinhamento registrada pelo Flyway.
6. Remova imediatamente as duas variáveis de manutenção.
7. Faça um novo deploy e confirme que a aplicação inicia normalmente e que todas as migrations são validadas.

## Resultado esperado e falhas seguras

- Caminho positivo: o checksum divergente é alinhado, uma mensagem de conclusão aparece e o processo termina.
- Confirmação ausente ou incorreta: o processo falha antes de abrir conexão com o banco.
- Configuração PostgreSQL ausente: o processo identifica apenas o nome da variável faltante e não expõe valores de credenciais.
- Modo desligado: nenhum código de reparo é executado e a inicialização normal do Spring continua.
