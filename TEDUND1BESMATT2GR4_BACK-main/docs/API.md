# API do Back-End — Clínica Médica Acadêmica (Grupo 4)

API REST com JSON (seção 4.2 do Documento de Requisitos e Arquitetura). O navegador **não** chama o Back-End diretamente: o Angular chama `/api` no próprio Front-End, e o proxy reverso (Nginx) repassa a requisição ao Back-End pela rede interna.

## Cabeçalhos

| Cabeçalho | Quem coloca | Para quê |
|---|---|---|
| `X-Frontend-Key` | o **proxy** do Front-End (nunca o código Angular) | O `FrontendOriginFilter` descarta com **403, sem corpo**, toda requisição sem a chave correta, e registra a tentativa na auditoria (RNF015). |
| `Authorization: Bearer <token>` | o interceptador HTTP do Angular | O JWT é obrigatório em todas as requisições, exceto no login (RNF014). |
| `Content-Type: application/json` | o Angular | Corpo das requisições e das respostas (RNF022). |

## Token (seção 6.1)

- `POST /api/auth/login` devolve o token (HS256), que vale **30 minutos** e traz o login, os perfis e a expiração.
- `POST /api/auth/renovar` troca o token atual por um novo, desde que o usuário continue ativo. O token antigo é revogado.
- `POST /api/auth/logout` grava o identificador (`jti`) do token em `tb_token_revogado`, e esse token passa a ser recusado com 401.
- Também é recusado com 401 o token de um usuário que foi inativado ou que está vinculado a um colaborador inativo (RN037).

## Métodos HTTP (seção 4.2.1)

| Método | Uso |
|---|---|
| GET | consultar registros e relatórios |
| POST | cadastrar e registrar operações (login, retirada, reposição, recepção, triagem, prescrição) |
| PUT | editar um registro (envie a `versao` que você leu) |
| PATCH | inativar, reativar ou mudar o status (aprovar, reprovar, iniciar, finalizar) |
| DELETE | **não existe**: nenhum registro com histórico é excluído (RN002, RN003) |

**Edição concorrente:** toda resposta traz `versao`. Na edição (PUT), envie a `versao` que leu. Se outra pessoa salvou nesse meio tempo, a API responde **409**.

## Paginação

As listas recebem os parâmetros `page` (começa em 0) e `size` (padrão 20, máximo 100) e respondem neste formato:

```json
{ "itens": [ ... ], "pagina": 0, "tamanho": 20, "totalItens": 57, "totalPaginas": 3 }
```

## Erros (seção 4.2.2)

| Código | Quando |
|---|---|
| 400 | dados inválidos ou incompletos (traz `campos` com o erro de cada campo) |
| 401 | sem token, ou token inválido, expirado ou revogado; login ou senha errados |
| 403 | perfil sem permissão (registrado na auditoria) ou requisição que não veio do Front-End |
| 404 | registro não encontrado |
| 409 | regra de negócio violada ou conflito de concorrência |
| 500 | erro inesperado (registrado no log, sem detalhes técnicos na resposta) |

```json
{
  "timestamp": "2026-10-16T14:32:10-03:00",
  "status": 409,
  "erro": "Regra de negócio violada",
  "mensagem": "Quantidade solicitada (15) maior que o saldo disponível (8).",
  "caminho": "/api/estoque/retiradas"
}
```

## Perfis (seção 6.2)

`ADM` = ADMINISTRADOR, `COL` = COLABORADOR, `USU` = USUARIO (atendente), `todos` = qualquer perfil autenticado. Quem acumula perfis recebe a soma das permissões (RN001).

## Endpoints

### Autenticação e controle de acesso — `/api/auth`, `/api/usuarios`, `/api/perfis`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| POST | `/api/auth/login` | público | `{ "login", "senha" }` → `{ token, tipo, expiraEm, usuario }` (RF044) |
| POST | `/api/auth/renovar` | todos | RenovarToken |
| POST | `/api/auth/logout` | todos | 204 — revoga o token (RF045) |
| GET | `/api/auth/me` | todos | quem está logado e seus perfis (o Front-End monta os menus — RF062) |
| PUT | `/api/auth/senha` | todos | `{ "senhaAtual", "novaSenha" }` |
| GET | `/api/usuarios` · `/{id}` | ADM | `?status=ATIVO&page&size` |
| POST | `/api/usuarios` | ADM | `{ login, senha, colaboradorId, perfis: ["USUARIO", ...] }` — o colaborador é obrigatório (RN037) |
| PUT | `/api/usuarios/{id}` | ADM | `{ novaSenha?, colaboradorId, perfis, versao }` (RF013) |
| PATCH | `/api/usuarios/{id}/inativar` · `/reativar` | ADM | `{ "motivo" }` na inativação |
| GET | `/api/perfis` | ADM | perfis disponíveis |

### Gestão administrativa — `/api/clinicas`, `/api/setores`, `/api/convenios`, `/api/colaboradores`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| GET/POST | `/api/clinicas` | ADM | RF001 — telefones e responsáveis são listas |
| GET/PUT | `/api/clinicas/{id}` | ADM | RF002 |
| PATCH | `/api/clinicas/{id}/inativar` · `/reativar` | ADM | RF003 — motivo obrigatório (RN023) |
| GET/POST/PUT/PATCH | `/api/setores...` | ADM | RF004 a RF006 — `clinicaId` e `responsavelId` obrigatórios (RN004, RN005) |
| GET | `/api/convenios` · `/{id}` | todos | o atendente escolhe o convênio do paciente |
| POST/PUT/PATCH | `/api/convenios...` | ADM | RF007 a RF009 |
| GET/POST/PUT/PATCH | `/api/colaboradores...` | ADM | RF010 a RF012, RF047, RF048 — médico exige especialidade, conselho e `horariosAtendimento` (RN024) |
| GET | `/api/colaboradores/medicos` | todos | médicos ativos (agendamento e paciente) |
| GET | `/api/especialidades` | todos | especialidades ativas |
| POST | `/api/especialidades` | ADM | `{ "nome" }` |

### Pacientes e prontuários — `/api/pacientes`, `/api/prontuarios`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| GET | `/api/pacientes` | todos | RF029 — `?nome=maria&status=ATIVO&page&size` |
| GET | `/api/pacientes/{id}` | todos | |
| POST | `/api/pacientes` | ADM, USU | RF027 — cria o paciente **e o prontuário** na mesma transação (RN007 a RN009) |
| PUT | `/api/pacientes/{id}` | ADM, USU | RF049; convênio exige `numeroCarteirinha` (RF052) |
| PATCH | `/api/pacientes/{id}/inativar` · `/reativar` | ADM, USU | RF050 — inativa também o prontuário |
| POST | `/api/pacientes/recepcao` | ADM, USU | RF028 — `{ "agendamentoId" }` → agendamento, paciente e prontuário |
| GET | `/api/pacientes/{id}/prontuario` | todos | RF030 — PEP completo (**consulta auditada**) |
| GET | `/api/prontuarios/{id}` | todos | RF030 — PEP completo (**consulta auditada**) |
| PATCH | `/api/prontuarios/{id}/inativar` · `/reativar` | ADM, USU | RF051, RN029 |

### Agendamento e atendimento clínico — `/api/agendamentos`, `/api/atendimentos`, `/api/orientacoes-enfermagem`, `/api/solicitacoes-medicamento`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| GET | `/api/agendamentos` | ADM, USU | RF026 — `?data=2026-10-20&medicoId&pacienteId&page&size` |
| GET | `/api/agendamentos/{id}` | ADM, USU | |
| POST | `/api/agendamentos` | ADM, USU | RF025 — começa AGENDADO; trava a agenda do médico (bloqueio pessimista) |
| PUT | `/api/agendamentos/{id}` | ADM, USU | RF053 |
| PATCH | `/api/agendamentos/{id}/inativar` · `/reativar` | ADM, USU | RF054, RN030 |
| POST | `/api/atendimentos` | COL (médico) | **Triagem**: `{ pacienteId, agendamentoId?, tipo, sintomas }` → etapa EM_TRIAGEM. O colaborador vem do login (RN028) |
| PATCH | `/api/atendimentos/{id}/iniciar` | COL | EM_TRIAGEM → EM_ATENDIMENTO |
| PUT | `/api/atendimentos/{id}` | COL | RF017 a RF019: `{ sintomas, diagnostico, tratamento, versao }` |
| POST | `/api/atendimentos/{id}/prescricoes` | COL | RF020: `{ "itens": [ { medicamentoId, dosagem, posologia } ] }` |
| PATCH | `/api/atendimentos/{id}/finalizar` | COL | RF032: `{ diagnostico?, tratamento?, internado }` → FINALIZADO, e o agendamento passa para FINALIZADO (RN010) |
| PATCH | `/api/atendimentos/{id}/inativar` | ADM, COL (responsável) | |
| GET | `/api/atendimentos` | ADM | todos (consulta) |
| GET | `/api/atendimentos/meus` · `/agenda` | COL | atendimentos e agenda do colaborador logado |
| GET | `/api/atendimentos/{id}` | ADM, COL | **consulta auditada** |
| POST | `/api/orientacoes-enfermagem` | COL | RF022: `{ atendimentoId, orientacao }` |
| GET | `/api/orientacoes-enfermagem?atendimentoId=` | ADM, COL | |
| POST | `/api/solicitacoes-medicamento` | COL | RF023: `{ medicamentoId, quantidade, justificativa, atendimentoId? }` → PENDENTE |
| GET | `/api/solicitacoes-medicamento/minhas` | COL | solicitações do próprio colaborador |

### Estoque e farmácia — `/api/medicamentos`, `/api/solicitacoes-medicamento`, `/api/estoque`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| GET | `/api/medicamentos` · `/{id}` | todos | RF034 — inclui `saldoAtual` e `reposicaoNecessaria` |
| POST | `/api/medicamentos` | ADM, USU | RF033 — `quantidadeEmEstoque`, `lote`, `dataValidade` e `dataAquisicao` viram a 1ª entrada |
| PUT | `/api/medicamentos/{id}` | ADM, USU | RF055, RF041 (`estoqueMinimo`, padrão 10) |
| PATCH | `/api/medicamentos/{id}/inativar` · `/reativar` | ADM, USU | RF056, RN012 |
| GET | `/api/solicitacoes-medicamento` | ADM, USU | fila: `?situacao=PENDENTE&page&size` |
| GET | `/api/solicitacoes-medicamento/{id}` | todos | o colaborador só vê as próprias |
| PATCH | `/api/solicitacoes-medicamento/{id}/aprovar` | ADM, USU | RF059, RN033 |
| PATCH | `/api/solicitacoes-medicamento/{id}/reprovar` | ADM, USU | `{ "motivo" }` |
| POST | `/api/estoque/retiradas` | ADM, USU | RF037 a RF040 — ver exemplo abaixo |
| POST | `/api/estoque/reposicoes` | ADM, USU | RF042: `{ medicamentoId, quantidade, lote, dataValidade, dataAquisicao, observacao }` |
| GET | `/api/estoque/saldos` | ADM, USU | RF057 |
| GET | `/api/estoque/reposicao` | ADM, USU | RF058 — saldo ≤ mínimo (10 unidades) |
| GET | `/api/estoque/movimentacoes` | ADM, USU | RF036 — `?medicamentoId&tipo=SAIDA&inicio=2026-10-01&fim=2026-10-31&page&size` |

Fluxo da solicitação: **PENDENTE → APROVADA ou REPROVADA → (retirada) → ENTREGUE**.

Exemplo de retirada (seção 4.2.3). O responsável pela entrega não é enviado, porque vem do token (RN018):

```http
POST /api/estoque/retiradas
{ "solicitacaoId": 42, "medicamentoId": 7, "quantidade": 2 }

HTTP/1.1 201 Created
{
  "movimentacaoId": 318, "tipo": "SAIDA", "medicamento": "Dipirona Sódica 500 mg", "quantidade": 2,
  "colaboradorSolicitante": "Ana Beatriz Souza", "responsavelEntrega": "carlos.dias",
  "dataHora": "2026-10-16T14:35:02-03:00", "saldoAtual": 10, "reposicaoNecessaria": true
}
```

### Relatórios e auditoria — `/api/relatorios`, `/api/auditoria`

| Método | Caminho | Perfis | Observação |
|---|---|---|---|
| GET | `/api/relatorios/administrativo` | ADM | RF015, RF061 — tudo, com o emissor identificado (RN034) |
| GET | `/api/relatorios/minha-movimentacao` | COL | RF024 — só a do próprio colaborador (RN020) |
| GET | `/api/relatorios/atendente` | ADM, USU | RF060 — cadastros, setores e solicitações aprovadas/reprovadas |
| GET | `/api/relatorios/estoque` | ADM, USU | RF043 — `?inicio=2026-10-01&fim=2026-10-31` |
| GET | `/api/auditoria` | ADM | RF014 — `?login&entidade&acao=LOGIN_FALHA&inicio&fim&page&size` |

## O que vai para a auditoria (seção 6.7)

| Origem | Ações registradas |
|---|---|
| automático, por ouvinte JPA | CRIAR, EDITAR, INATIVAR, REATIVAR e MUDAR_STATUS, com o que mudou. Dados clínicos e pessoais aparecem só pelo nome do campo. |
| automático, por aspecto | CONSULTAR (prontuário e atendimento) e EMITIR_RELATORIO |
| eventos de segurança | LOGIN, LOGIN_FALHA, LOGOUT, RENOVAR_TOKEN, ORIGEM_INVALIDA e ACESSO_NEGADO |

Cada registro guarda o usuário, a data e hora, o IP de origem, a ação, o resultado (SUCESSO, FALHA ou NEGADO) e o registro afetado. A gravação é assíncrona, e o usuário de banco da aplicação não tem permissão para alterar nem excluir registros de auditoria (RN036).
