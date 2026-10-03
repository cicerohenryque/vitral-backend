# Histórico técnico e funcional — Sprints 1 a 3

Este documento registra as entregas incorporadas à branch
`sprint3/prioritized-improvements`. Ele complementa o README operacional e a
documentação OpenAPI disponível no Swagger.

## Visão consolidada

| Sprint | Entregas principais |
| --- | --- |
| Sprint 1 | Mensagens e validações da API mais claras e alinhamento dos testes integrados. |
| Sprint 2 | Busca e filtros, recomendações, desempenho, preço efetivo e administração com auditoria. |
| Sprint 3 | Paginação segura, verificação de sebos, localização, regressões, testes e documentação. |

## Sprint 1 — Feedback da API

- Mensagens de validação dos DTOs foram reescritas para identificar o campo ou a
  ação que precisa ser corrigida.
- Regras de autenticação, cadastro, perfil, categorias, produtos, ofertas,
  pedidos, mensagens, suporte, upload e verificação de sebos passaram a produzir
  respostas de erro mais específicas.
- O tratamento global diferencia erros de negócio, validação, autenticação,
  autorização, recurso inexistente e falha inesperada sem expor detalhes
  internos ao cliente.
- Serviços passaram a informar por que uma operação não pôde ser concluída e,
  quando aplicável, qual estado impede a ação.
- Massa e cenários E2E foram alinhados ao esquema e aos fluxos atuais.

## Sprint 2 — Catálogo, recomendações e administração

### Busca, filtros e apresentação

- `GET /api/v1/busca/produtos` aceita termo, sebo, condição, preço mínimo/máximo,
  categoria, gênero, ordenação e paginação.
- A pesquisa textual normaliza caixa e acentos no PostgreSQL sem carregar o
  catálogo em memória.
- Filtros são executados por `Specification`; produtos inativos, sem estoque ou
  pertencentes a contas/sebos indisponíveis não são tratados como candidatos
  públicos.
- A ordenação por preço usa o preço efetivo: oferta ativa e válida quando houver,
  ou preço normal do produto. Há teste de repositório para esse comportamento.
- Consultas da Home foram limitadas às quantidades necessárias para cada seção,
  reduzindo transferência e processamento desnecessários.

### Recomendações personalizadas

- O perfil de interesse agrega eventos recentes por tipo de produto, categoria,
  gênero, autor/artista, faixa de preço, sebo e termo pesquisado.
- Cada interação recebe o peso-base do tipo de evento e decaimento temporal;
  eventos recentes influenciam mais que eventos antigos.
- Favoritos, itens já presentes na cesta e compras recentes são excluídos para
  reduzir repetição. Produtos indisponíveis também não são recomendados.
- O ranking combina afinidade, termos pesquisados, popularidade e recência do
  produto. Os pesos possuem defaults seguros e podem ser configurados pelas
  propriedades `app.recomendacao.pesos` documentadas no README.
- A seleção aplica diversidade entre interesses em vez de preencher toda a
  página com itens equivalentes.
- Usuários sem histórico suficiente recebem fallback de itens disponíveis,
  populares e recentes.
- O desempate usa score, popularidade, data de criação e identificador, mantendo
  a ordem determinística com os mesmos dados.
- A obtenção de histórico, favoritos, cesta, compras, candidatos e popularidade
  é feita em consultas agregadas, evitando consulta individual por produto.
- A migration `V40` cria o índice parcial usado pelo fallback de produtos ativos
  e disponíveis, ordenado por data e identificador.

### Administração e segurança

- Todas as rotas `/api/v1/admin/**` exigem `ROLE_ADMIN`.
- O dashboard apresenta totais de usuários, sebos, produtos e pedidos por status.
- Há listagens paginadas e filtráveis de usuários, produtos, sebos, pedidos e
  auditoria, além de consulta individual de usuários e produtos.
- Administradores podem ativar/desativar contas não administrativas e produtos.
  Uma conta administrativa não pode desativar a si mesma nem outra conta admin.
- As respostas administrativas são DTOs dedicados e não expõem senha, hash,
  token ou credenciais sensíveis.
- Alterações administrativas são registradas em `admin_audit`; a migration
  `V41` cria a tabela e seus índices de consulta.
- O primeiro administrador pode ser criado por variáveis de bootstrap. Não há
  credencial administrativa padrão nem cadastro público de `ADMIN`.

Principais endpoints:

| Método | Endpoint | Finalidade |
| --- | --- | --- |
| `GET` | `/api/v1/admin/dashboard` | Métricas gerais. |
| `GET` | `/api/v1/admin/usuarios` | Lista e filtra contas. |
| `PUT` | `/api/v1/admin/usuarios/{id}/status` | Ativa ou desativa uma conta permitida. |
| `GET` | `/api/v1/admin/produtos` | Lista e filtra produtos. |
| `PUT` | `/api/v1/admin/produtos/{id}/status` | Ativa ou desativa produto. |
| `GET` | `/api/v1/admin/sebos` | Lista sebos e situação de verificação. |
| `GET` | `/api/v1/admin/pedidos` | Consulta pedidos por filtros. |
| `GET` | `/api/v1/admin/auditoria` | Consulta ações administrativas. |

## Sprint 3 — Robustez, localização e documentação

### Melhorias priorizadas

- A paginação web possui tamanho padrão de 20 e máximo de 100 itens, configurável
  por ambiente, evitando requisições excessivas.
- A ordenação por proximidade ganhou desempate pelo identificador, tornando o
  resultado estável.
- O fluxo normal de novos sebos mantém o cadastro `PENDENTE` para revisão
  administrativa. Aprovação automática passou a ser uma opção explícita apenas
  para desenvolvimento e permanece desabilitada por padrão.

### Localização e descoberta de sebos

- `GET /api/v1/busca/sebos` aceita `q`, `cidade`, `uf`, `lat`, `lng`, `raioKm`,
  `page` e `size` sem exigir autenticação.
- Latitude e longitude devem ser enviadas juntas, dentro dos limites geográficos
  e com valores finitos. O raio deve ser finito, maior que zero e de até 500 km.
- Com raio, uma `Specification` aplica um retângulo geográfico no banco para
  reduzir candidatos. O serviço usa Haversine para o corte exato e ordena por
  distância e identificador.
- Sem raio, coordenadas apenas ordenam os sebos; registros sem coordenadas ficam
  ao final. Com raio, registros sem coordenadas são excluídos.
- A resposta inclui `distanciaKm` quando ela puder ser calculada. A posição do
  usuário não é persistida.
- A assinatura anterior do serviço foi preservada por sobrecarga e o novo
  parâmetro HTTP é opcional, mantendo compatibilidade com clientes existentes.

### Correções de regressão

- Coordenadas incompletas, `NaN`, infinito, limites inválidos e raio sem posição
  são rejeitados com `400 Bad Request` em vez de chegar ao cálculo geográfico.
- Filtros paginados e ordenações determinísticas receberam cenários de regressão.

### Testes atualizados

- Recomendação: personalização, decaimento temporal, pesos, fallback, diversidade,
  desempate e ausência de itens bloqueados/indisponíveis.
- Busca: normalização textual, filtros, preço efetivo, proximidade, raio, limites
  geográficos, paginação e desempate.
- Administração: autorização, bootstrap, listagens, status e auditoria.
- Serviços afetados pelas novas mensagens e regras continuam cobertos por testes
  unitários, de repositório e integração de segurança.

## Banco de dados

| Migration | Alteração |
| --- | --- |
| `V40__indice_recomendacao_produtos_recentes.sql` | Índice parcial para o fallback de recomendações disponíveis e recentes. |
| `V41__create_admin_audit.sql` | Tabela e índices da auditoria administrativa. |

As coordenadas do sebo usam os campos já existentes no cadastro. A localização
do usuário não cria tabela, coluna ou histórico adicional.

## Validação da entrega

Execute antes de publicar alterações:

```bash
./mvnw clean test
```

Na validação desta branch, 369 testes do backend foram aprovados. O número é um
retrato desta entrega e pode aumentar com novos testes.

## Limites conhecidos

- A recomendação é heurística e não utiliza machine learning nem cache
  distribuído.
- O cálculo Haversine exato é feito em memória somente após o pré-filtro
  geográfico no banco. É adequado ao MVP, mas bases muito maiores podem exigir
  recursos geoespaciais nativos.
- A qualidade da descoberta depende de os sebos possuírem coordenadas geradas a
  partir de um endereço válido.
