# Vitral Backend

API do projeto Vitral, desenvolvida com Java 21, Spring Boot, PostgreSQL e Flyway.

O histórico técnico e funcional das Sprints 1, 2 e 3 está em
[docs/SPRINTS.md](docs/SPRINTS.md).

## Pré-requisitos

- Git
- Java 21
- Docker com Docker Compose v2

No Windows e no macOS, a opção mais simples é instalar o Docker Desktop. No Linux, instale o Docker Engine e o plugin Docker Compose.

Confirme as instalações:

```bash
java -version
docker --version
docker compose version
```

## Portas usadas

- Backend: `8080`
- PostgreSQL: `5432`
- Swagger: <http://localhost:8080/swagger-ui.html>

## Opção A — Subir tudo em containers (banco + backend)

O `docker-compose.yml` sobe o PostgreSQL **e** o backend (imagem construída pelo `Dockerfile`). O backend só inicia depois que o banco fica saudável (`healthcheck` + `depends_on`), e as imagens enviadas ficam em um volume persistente.

### Linux e macOS

```bash
export POSTGRES_PASSWORD="vitral_dev"
export JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
docker compose up --build -d
docker compose ps
```

### Windows — PowerShell

```powershell
$env:POSTGRES_PASSWORD="vitral_dev"
$env:JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
docker compose up --build -d
docker compose ps
```

Quando `vitral-backend` estiver `Up`, acesse <http://localhost:8080> e <http://localhost:8080/swagger-ui.html>. Para parar tudo: `docker compose down` (adicione `-v` para apagar banco e uploads).

## Opção B — Banco em Docker + backend local (Maven Wrapper)

Use esta opção para desenvolvimento com hot reload. Suba **apenas** o banco e rode o backend pelo Maven Wrapper.

### 1. Iniciar o PostgreSQL com Docker

### Windows — PowerShell

```powershell
cd C:\caminho\para\vitral-backend
$env:POSTGRES_PASSWORD="vitral_dev"
docker compose up -d db
docker compose ps
```

### Windows — Prompt de Comando

```bat
cd C:\caminho\para\vitral-backend
set POSTGRES_PASSWORD=vitral_dev
docker compose up -d db
docker compose ps
```

### Linux e macOS

```bash
cd /caminho/para/vitral-backend
export POSTGRES_PASSWORD="vitral_dev"
docker compose up -d db
docker compose ps
```

O banco será criado com estes valores padrão:

```text
Banco:   vitral_db
Usuário: vitral_user
Senha:   valor definido em POSTGRES_PASSWORD
```

### 2. Executar o backend

O segredo abaixo serve somente para desenvolvimento local. Em produção, utilize um segredo exclusivo e armazenado de forma segura.

> O `JWT_SECRET` precisa estar em **Base64 padrão** (o backend o decodifica com `Decoders.BASE64`) e ter no mínimo 32 bytes. Não use Base64 *URL-safe*: caracteres como `-` e `_` são inválidos e causam o erro `Illegal base64 character` na inicialização. Para gerar um válido: `openssl rand -base64 32`.

### Windows — PowerShell

Na mesma janela usada para iniciar o banco:

```powershell
$env:JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
.\mvnw.cmd spring-boot:run
```

### Windows — Prompt de Comando

Na mesma janela usada para iniciar o banco:

```bat
set JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=
mvnw.cmd spring-boot:run
```

### Linux e macOS

No mesmo terminal usado para iniciar o banco:

```bash
export JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
./mvnw spring-boot:run
```

Quando a mensagem `Started VitralBackendApplication` aparecer, acesse:

- API: <http://localhost:8080>
- Swagger: <http://localhost:8080/swagger-ui.html>

Para interromper o backend, pressione `Ctrl+C`.

## Executar os testes

### Windows

```powershell
.\mvnw.cmd test
```

### Linux e macOS

```bash
./mvnw test
```

O teste do filtro geográfico em PostgreSQL real usa Testcontainers e fica em um
perfil separado para que a suíte unitária não dependa de Docker:

```bash
./mvnw -Ppostgres-it verify
```

O workflow **Backend CI** executa a suíte padrão e esse perfil de integração em
jobs separados.

### Testes de carga

O cenário k6 em [load-tests/catalog.js](load-tests/catalog.js) mede Home, busca e
recomendações com limites próprios de latência e erro. As instruções, variáveis
e cuidados para execução estão em [load-tests/README.md](load-tests/README.md).

## Busca e descoberta de sebos

A busca pública de sebos está disponível em:

```http
GET /api/v1/busca/sebos
```

Parâmetros opcionais:

| Parâmetro | Descrição |
| --- | --- |
| `q` | Parte do nome do sebo. |
| `cidade` | Parte do nome da cidade. |
| `uf` | UF do endereço. |
| `lat` | Latitude do usuário, entre -90 e 90. |
| `lng` | Longitude do usuário, entre -180 e 180. |
| `raioKm` | Distância máxima em quilômetros, maior que zero e de até 500. |
| `page` | Página iniciada em zero. |
| `size` | Quantidade de itens por página. |

Exemplo para descobrir sebos em um raio de 25 km:

```http
GET /api/v1/busca/sebos?lat=-7.115&lng=-34.8631&raioKm=25&page=0&size=20
```

`lat` e `lng` devem ser informados juntos. `raioKm` só pode ser usado quando as
duas coordenadas estiverem presentes. Entradas incompletas, não finitas ou fora
dos limites retornam `400 Bad Request` com uma mensagem de validação.

Com coordenadas, a resposta é ordenada pela distância calculada até o sebo e,
em caso de empate, pelo identificador. O campo `distanciaKm` é incluído em cada
item que possua coordenadas. Sem coordenadas, o endpoint preserva a busca
paginada existente por nome, cidade e UF.

Quando um raio é informado, o banco aplica primeiro um retângulo geográfico para
reduzir os candidatos. Em seguida, o serviço calcula a distância Haversine e
remove os resultados que ultrapassam o raio exato. Essa estratégia evita
calcular a distância de todos os sebos cadastrados.

O endpoint não armazena a localização recebida. Sebos sem coordenadas não entram
em resultados limitados por raio e aparecem ao final quando a ordenação por
proximidade é usada sem limite.

Cada busca por proximidade registra `candidatos`, `resultados`, `raioKm` e
`duracaoMs`. Consultas com mais de 500 candidatos ou duração superior a 500 ms
também geram um aviso `busca_proximidade_lenta`, permitindo acompanhar quando a
estratégia do MVP precisar ser revista.

A geocodificação registra resultados de sucesso, endereço incompleto, ausência
de resultado e erro sem incluir o endereço completo no log. O painel
administrativo informa se cada sebo possui localização disponível e permite
tentar novamente depois que o endereço for corrigido.

## Gerar e executar o arquivo JAR

### Windows

```powershell
.\mvnw.cmd clean package
java -jar target\vitral-backend-0.0.1-SNAPSHOT.jar
```

### Linux e macOS

```bash
./mvnw clean package
java -jar target/vitral-backend-0.0.1-SNAPSHOT.jar
```

As variáveis `POSTGRES_PASSWORD` e `JWT_SECRET` precisam continuar definidas no terminal que executar o JAR.

## Comandos úteis do Docker

Execute os comandos na pasta do backend e mantenha `POSTGRES_PASSWORD` definida no terminal.

```bash
# Ver o estado do banco
docker compose ps

# Acompanhar os logs
docker compose logs -f db

# Parar e remover o contêiner, preservando os dados
docker compose down

# Reiniciar o banco
docker compose restart db
```

Para apagar também o volume e todos os dados locais do banco:

```bash
docker compose down -v
```

> Atenção: `docker compose down -v` remove permanentemente o banco local.

## Configuração opcional de e-mail

O envio de e-mail fica desabilitado por padrão. Para ativá-lo, defina estas variáveis antes de iniciar o backend:

```text
APP_MAIL_ENABLED=true
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email
SPRING_MAIL_PASSWORD=sua-senha-de-aplicativo
```

Também podem ser configuradas:

```text
APP_URL_BASE=http://localhost:8080
APP_URL_FRONTEND=http://localhost:5173
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

## Criação inicial do administrador

Para criar o primeiro administrador, defina as variáveis abaixo antes de iniciar a aplicação:

```text
APP_BOOTSTRAP_ADMIN_EMAIL=admin@exemplo.com
APP_BOOTSTRAP_ADMIN_PASSWORD=uma-senha-forte
APP_BOOTSTRAP_ADMIN_NAME=Administrador
```

A criação ocorre somente quando ainda não existe uma conta `ADMIN`. Depois do primeiro acesso, remova as variáveis
do ambiente. A senha nunca é armazenada em texto puro e esse perfil não pode ser criado pelo cadastro público.

## Variáveis de ambiente adicionais

Todas as configurações sensíveis ou de ambiente são lidas por variáveis:

```text
# Origens permitidas no CORS (separadas por vírgula). Em produção, use o domínio real do frontend.
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173

# Diretório onde as imagens enviadas são gravadas.
# No Docker (Opção A) já aponta para o volume persistente /data/uploads/images.
APP_UPLOAD_IMAGE_DIR=uploads/images

# Paginação das listagens. Requisições acima do máximo são limitadas pelo Spring.
APP_PAGE_DEFAULT_SIZE=20
APP_PAGE_MAX_SIZE=100

# Limites para destacar buscas por proximidade que precisam de análise.
APP_BUSCA_PROXIMIDADE_WARN_CANDIDATOS=500
APP_BUSCA_PROXIMIDADE_WARN_DURACAO_MS=500

# Somente para desenvolvimento local: aprova sebos sem revisão administrativa.
APP_SEBO_VERIFICACAO_MOCK_AUTO_APROVAR=false
```

O fluxo padrão mantém novos sebos com status `PENDENTE` até a revisão por um
administrador. `APP_SEBO_VERIFICACAO_MOCK_AUTO_APROVAR` deve permanecer `false`
em produção; habilite-a apenas quando um ambiente local precisar ignorar
temporariamente a revisão. Alterações de CNPJ ou documento fazem o cadastro
voltar ao fluxo de verificação.

Os limites de paginação protegem as listagens contra requisições acidentais com
milhares de registros. O padrão é 20 itens e o máximo aceito é 100 itens por
página. Esses valores podem ser ajustados por ambiente sem recompilar a
aplicação.

### Pesos opcionais das recomendações

O sistema possui valores padrão seguros. As variáveis abaixo só precisam ser definidas quando for necessário ajustar
o ranking sem recompilar a aplicação:

```text
APP_RECOMENDACAO_HISTORICO_MAXIMO=1000
APP_RECOMENDACAO_CANDIDATOS_MAXIMO=200
APP_RECOMENDACAO_TAMANHO_HOME=20
APP_RECOMENDACAO_RETENCAO_TERMO_DIAS=30
APP_RECOMENDACAO_RETENCAO_EVENTO_DIAS=365
APP_RECOMENDACAO_JANELA_VISUALIZACAO_MINUTOS=30
APP_RECOMENDACAO_EVENTOS_REPETIDOS_DIA_MAXIMO=3
APP_RECOMENDACAO_COMPRA_RECENTE_DIAS=90
APP_RECOMENDACAO_TAMANHO_PAGINA_MAXIMO=50
APP_RECOMENDACAO_POPULARIDADE_DIAS=90
```

- `HISTORICO_MAXIMO` e `CANDIDATOS_MAXIMO` limitam o trabalho feito por requisição.
- `TAMANHO_HOME` limita a seção de recomendações da Home.
- As retenções definem por quanto tempo termos e eventos influenciam o perfil.
- A janela e o limite de repetição reduzem eventos duplicados de visualização.
- `COMPRA_RECENTE_DIAS` evita recomendar novamente compras recentes.
- `TAMANHO_PAGINA_MAXIMO` protege o endpoint público de recomendações.
- `POPULARIDADE_DIAS` define a janela usada no componente de popularidade.

Os pesos do ranking também podem ser ajustados:

```text
APP_RECOMENDACAO_PESO_CATEGORIA=1.5
APP_RECOMENDACAO_PESO_AUTOR=1.2
APP_RECOMENDACAO_PESO_FAIXA_PRECO=0.5
APP_RECOMENDACAO_PESO_SEBO=0.2
APP_RECOMENDACAO_PESO_TERMO_PESQUISA=0.8
APP_RECOMENDACAO_PESO_POPULARIDADE=0.15
APP_RECOMENDACAO_PESO_LANCAMENTO=1.0
APP_RECOMENDACAO_PESO_PRODUTO_ATE30_DIAS=2.0
APP_RECOMENDACAO_PESO_PRODUTO_ATE90_DIAS=1.0
APP_RECOMENDACAO_PESO_PRODUTO_ATE180_DIAS=0.5
```

Valores numéricos iguais ou menores que zero ou não finitos são substituídos pelos defaults acima. Valores que não
sejam números impedem a inicialização, permitindo detectar uma configuração incorreta imediatamente.

Nunca envie senhas reais ou segredos JWT para o Git.

## Solução de problemas

### A porta 5432 já está em uso

Pare outro PostgreSQL local ou altere a porta publicada no `docker-compose.yml`.

### O Docker informa que `POSTGRES_PASSWORD` não foi definida

Defina a variável na mesma janela do terminal em que executar `docker compose`.

### O backend não conecta ao banco

Confira se o contêiner está ativo:

```bash
docker compose ps
docker compose logs db
```

Por padrão, o backend usa `jdbc:postgresql://localhost:5432/vitral_db` com o usuário `vitral_user`.
