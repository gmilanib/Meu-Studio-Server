# Manutenção de clientes e faturamentos

## Decisões de domínio

- Faturamentos armazenam data e horário locais de `America/Sao_Paulo`. O horário pode ser escolhido pela usuária; quando omitido na API, assume o minuto atual desse fuso.
- A combinação de data e horário não pode estar no futuro. A migração V10 atribui `00:00` aos lançamentos anteriores e torna a coluna obrigatória.
- Faturamentos aceitam cliente cadastrado ou texto livre.
- `faturamentos.cliente_id` é opcional e representa o vínculo confiável com `clientes.id`.
- Quando há vínculo, a API responde com o nome atual do cliente. Assim, renomear um cliente atualiza sua apresentação em todos os lançamentos vinculados sem reescrever o histórico textual.
- Quando não há vínculo, `clientename` continua sendo a fonte do nome exibido.
- Um cliente com faturamentos vinculados não pode ser excluído. Primeiro, cada lançamento deve ser transferido para outro cliente, convertido em texto livre ou excluído.

## Migração V9

A migração adiciona a chave estrangeira anulável e tenta associar dados antigos somente quando existe exatamente um cliente com o mesmo nome normalizado por caixa e espaços externos. Nomes sem correspondência e nomes duplicados permanecem como texto livre para evitar associação ambígua.

## Contratos e interface

- `FaturamentoRequest.clienteId` é opcional.
- Sem `clienteId`, o backend associa automaticamente somente quando encontra um único nome equivalente, ignorando caixa e espaços externos; zero ou múltiplas correspondências mantêm texto livre.
- `FaturamentoResponse` informa `clienteId` e `procedimentoId` para permitir edição segura.
- `PUT /financeiro/faturamentos/{id}` altera todos os campos, inclusive `horario` no formato `HH:mm`.
- `DELETE /financeiro/faturamentos/{id}` remove definitivamente o lançamento.
- As tabelas de clientes e faturamentos abrem um modal de edição ou exclusão e exigem uma segunda confirmação antes da chamada de escrita.
- A edição de cliente preserva o e-mail atual sem mostrá-lo no modal.
- Depois de uma escrita bem-sucedida, a tela mostra uma mensagem e recarrega tabela e gráfico com os filtros atuais.

## Testes relevantes

- Backend: `ClienteManutencaoTest`, `FinanceiroManutencaoTest` e `FaturamentoConsultaTest` cobrem sucesso, bloqueios, horário padrão, instante futuro, ordenação, texto livre e IDs inexistentes.
- Frontend: `RelatorioClientes.test.jsx`, `RelatorioFinanceiro.test.jsx` e `Financeiro.test.jsx` cobrem confirmação, horário, preservação do e-mail, vínculo e aviso de texto livre.
