# Consulta de faturamentos

O contrato completo e a referência principal para integração do frontend estão em [API.md, na raiz do projeto](../API.md#consultar-faturamentos). Atualize esse arquivo sempre que o endpoint mudar.

`GET /financeiro/faturamentos` retorna JSON e usa a autenticação existente da API.

Todos os filtros são opcionais e combinados com E:

| Parâmetro | Comparação |
| --- | --- |
| cliente | Trecho literal, sem distinguir maiúsculas e minúsculas |
| procedimento | Trecho literal, sem distinguir maiúsculas e minúsculas |
| valor | Valor exato, com ponto decimal |
| meioDePagamento | Texto exato |
| data | Dia exato, formato YYYY-MM-DD |
| dataInicio | Data mínima inclusiva |
| dataFim | Data máxima inclusiva |

Textos em branco são ignorados; espaços nas extremidades são removidos.
Não combine `data` com `dataInicio` ou `dataFim`. Um limite do intervalo pode ser omitido.
Intervalos invertidos e paginação inválida retornam HTTP 400.

`page` começa em zero (padrão: 0). `size` aceita de 1 a 50 (padrão: 50).
Sem filtros, retorna todos os faturamentos, paginados. Ordenação: data decrescente, com identificador decrescente como desempate.

Exemplo: `/financeiro/faturamentos?cliente=maria&dataInicio=2026-09-01&dataFim=2026-09-10&page=0&size=50`

```json
{
  "content": [],
  "page": 0,
  "size": 50,
  "totalElements": 0,
  "totalPages": 0
}
```

Cada item de `content` contém `id`, `data`, `cliente`, `procedimento`, `valor` e `meioDePagamento`, como na resposta de lançamento.
