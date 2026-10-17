# Clínica Médica Acadêmica — Back-End (Grupo 4)

Back-End em **Java 21 + Spring Boot 4**, com **Spring Security** (usuários e perfis no PostgreSQL) e **PostgreSQL** versionado por **Flyway**. Segue o *Documento de Requisitos e Arquitetura* do grupo.

## Arquitetura Limpa (seções 3 e 5.2)

O projeto tem seis módulos: `autenticacao`, `administracao`, `pacientes`, `atendimento`, `estoque` e `relatorios`. Cada um tem as mesmas camadas:

```
br.ucsal.clinica.<modulo>
├── dominio/                  entidades e regras de negócio em Java puro (sem Spring, sem JPA)
├── aplicacao/                casos de uso, um por classe (ex.: RegistrarRetiradaMedicamento)
├── porta/                    interfaces que o núcleo precisa (repositórios, SecurityContextPort...)
└── adaptador/
    ├── web/                  controllers REST e DTOs (entrada)
    └── persistencia/         entidades JPA, repositórios Spring Data e mapeadores (saída)

br.ucsal.clinica.seguranca      SecurityConfig, FrontendOriginFilter, JWT, UserDetailsService
br.ucsal.clinica.auditoria      aspecto e ouvintes JPA de auditoria
br.ucsal.clinica.compartilhado  base do domínio, tratamento global de erros, configurações
```

As dependências apontam para dentro: **Controller → Caso de Uso → Domínio ← Porta ← Adaptador JPA → PostgreSQL**.

- Os casos de uso não têm anotações do Spring. Eles viram beans pela `CasosDeUsoConfig`, e cada um roda numa transação aplicada por configuração (`TransacaoCasosDeUsoConfig`).
- Os testes em `src/test/java` exercitam o domínio e os casos de uso com portas em memória, sem banco e sem Spring.

## Como rodar no Eclipse

1. PostgreSQL rodando com o banco `clinica` criado.
2. Na raiz do projeto, o arquivo **`local.properties`** guarda as senhas e os segredos. Ele fica fora do GitHub (`.gitignore`), e o modelo é o `local.properties.exemplo`.
3. Rode a classe `br.ucsal.clinica.ClinicaApplication`. O Flyway cria as tabelas (V1), os dados iniciais (V2), os dados de teste (V3) e o **usuário restrito `clinica_app`** (V4), que é o usuário com que a aplicação conecta.
4. Teste mandando o cabeçalho `X-Frontend-Key` com o valor de `FRONTEND_KEY`:

```powershell
$h = @{ "X-Frontend-Key" = "<FRONTEND_KEY do local.properties>" }
$r = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -Headers $h `
     -ContentType "application/json" -Body '{"login":"admin","senha":"admin123"}'
$h["Authorization"] = "Bearer " + $r.token
Invoke-RestMethod -Uri http://localhost:8080/api/auth/me -Headers $h
```

Usuários de teste (senha `senha123`): `ana.souza` e `marcos.lima` (médicos, COLABORADOR), `carlos.dias` e `paula.ribeiro` (USUARIO), `joao.santos` (ADMINISTRADOR + USUARIO). O administrador inicial é `admin` / `admin123`.

No desenvolvimento com Angular (`ng serve`), use o proxy do Angular para repassar `/api` e acrescentar a chave (modelo em `deploy/angular-proxy.conf.json.exemplo`). Assim a chave nunca entra no código do navegador.

## Implantação com Docker (seção 7.1)

```bash
cp .env.exemplo .env                # preencha os segredos
# coloque o build do Angular em deploy/frontend-dist e o certificado em deploy/certs
docker compose up --build
```

- Só o **Nginx do Front-End** publica portas (443/80). O Back-End e o banco ficam na rede interna, sem portas publicadas.
- O Nginx repassa `/api` ao Back-End e acrescenta o `X-Frontend-Key`, sobrescrevendo qualquer valor que venha do navegador.
- O `pg_hba.conf` do banco só aceita conexões vindas do endereço do Back-End.

## Banco de dados

- `src/main/resources/db/migration`: V1 (tabelas, regras e views), V2 (dados obrigatórios) e V4 (usuário restrito da aplicação).
- `src/main/resources/db/testData`: V3 (dados de teste da apresentação).
- `testes-banco.sql`: 22 testes das regras garantidas pelo banco. Rode com o usuário `clinica_app`.

A documentação da API está em [`docs/API.md`](docs/API.md).
