# Manutenção de clientes e faturamentos

## Decisões de domínio

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
- `PUT /financeiro/faturamentos/{id}` altera todos os campos.
- `DELETE /financeiro/faturamentos/{id}` remove definitivamente o lançamento.
- As tabelas de clientes e faturamentos abrem um modal de edição ou exclusão e exigem uma segunda confirmação antes da chamada de escrita.
- A edição de cliente preserva o e-mail atual sem mostrá-lo no modal.
- Depois de uma escrita bem-sucedida, a tela mostra uma mensagem e recarrega tabela e gráfico com os filtros atuais.

## Testes relevantes

- Backend: `ClienteManutencaoTest` e `FinanceiroManutencaoTest` cobrem sucesso, bloqueios, texto livre e IDs inexistentes.
- Frontend: `RelatorioClientes.test.jsx`, `RelatorioFinanceiro.test.jsx` e `Financeiro.test.jsx` cobrem confirmação, preservação do e-mail, vínculo e aviso de texto livre.
