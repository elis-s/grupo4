# Front-End — Clínica Médica Acadêmica

Aplicação Angular 20 que consome a API Java/Spring Boot do projeto.

## O que tem

- Login real (`POST /api/auth/login`), sessão no `sessionStorage` e logout que revoga o token no Back-End.
- Menu lateral único, montado pelos **perfis** do usuário (ADMINISTRADOR, USUARIO, COLABORADOR). Rotas protegidas por perfil.
- Se a API responder **401** (token vencido ou revogado), o front encerra a sessão e volta ao login com aviso.
- Painel com indicadores (medicamentos para repor, solicitações pendentes, minha agenda).
- Telas de consulta com filtros, paginação, datas em `dd/mm/aaaa` e status em "badges":
  pacientes, agendamentos, minha agenda, atendimentos, medicamentos, reposição de estoque, movimentações,
  solicitações de medicamento (com **Aprovar/Reprovar**), colaboradores, convênios, clínicas, setores, usuários e auditoria.
- Responsivo (menu vira gaveta no celular).

Ainda **não** tem formulários de cadastro/edição, triagem/prescrição, retirada/reposição de estoque nem relatórios.

## Como adicionar uma tela nova

Tudo está em `src/app/core/resources.ts`: acrescente um item em `RESOURCES` (rota, perfis, endpoint por perfil, colunas, filtros).
A rota, o menu e o card do painel aparecem sozinhos.

## Requisitos

- Node.js 20.19+ ou 22.12+ (`node -v`).
- Back-End e PostgreSQL rodando (README da raiz).

## Rodar no Windows

```powershell
cd frontend
npm install
Copy-Item proxy.conf.json.example proxy.conf.json
# edite proxy.conf.json e troque o valor de X-Frontend-Key pelo FRONTEND_KEY do local.properties
npm start
```

Abra http://localhost:4200. O `proxy.conf.json` está no `.gitignore`: **não** envie ao GitHub.

Logins de teste: `admin` / `admin123`; `ana.souza` (colaborador), `carlos.dias` (usuário/atendente), `joao.santos` (admin + usuário), todos com `senha123`.

## Gerar para o Nginx/Docker

```powershell
npm run build
```

Grava em `../deploy/frontend-dist` (pasta servida pelo Nginx). Não versione o resultado do build.

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Login diz "Acesso recusado pelo Back-End" (403) | `X-Frontend-Key` do `proxy.conf.json` diferente de `FRONTEND_KEY` do `local.properties` |
| "Não foi possível acessar o servidor" | Back-End fora do ar (porta 8080) ou `npm start` sem `proxy.conf.json` |
| `npm start` reclama que não acha o proxy | Faltou copiar `proxy.conf.json.example` para `proxy.conf.json` |
| 403 numa tela | O perfil do usuário não tem permissão (regra do Back-End) |
