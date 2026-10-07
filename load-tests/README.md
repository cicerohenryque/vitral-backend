# Testes de carga do catálogo

O cenário k6 cobre, em paralelo, a Home, a busca de produtos e as recomendações
personalizadas. Ele usa uma taxa constante para permitir comparação entre
execuções e falha quando:

- mais de 1% das requisições apresentam erro;
- o p95 da Home ultrapassa 750 ms;
- o p95 da busca ultrapassa 900 ms;
- o p95 das recomendações ultrapassa 1 segundo;
- o ambiente não sustenta a taxa configurada e descarta iterações.

## Execução

Instale o [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/), inicie o
backend com uma base representativa e gere um token de uma conta `USUARIO`.

```bash
VITRAL_TOKEN="token-jwt" k6 run load-tests/catalog.js
```

As variáveis disponíveis são:

- `BASE_URL`: padrão `http://localhost:8080`;
- `VITRAL_TOKEN`: obrigatório para o cenário de recomendações;
- `RATE`: requisições por segundo **em cada cenário**, padrão `5`;
- `DURATION`: duração, padrão `30s`;
- `PRE_ALLOCATED_VUS`: usuários virtuais iniciais, padrão `10`;
- `MAX_VUS`: limite de usuários virtuais, padrão `50`.

Exemplo de uma carga mais intensa em homologação:

```bash
BASE_URL="https://api-homologacao.exemplo.com" \
VITRAL_TOKEN="token-jwt" RATE=20 DURATION=2m MAX_VUS=100 \
k6 run load-tests/catalog.js
```

Não execute contra produção sem autorização. Compare p95, taxa de erro e
iterações descartadas com uma execução de referência feita sobre volume de dados
semelhante. Tokens nunca devem ser adicionados ao Git.
