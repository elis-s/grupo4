import { ColType } from './format';

export interface Column { key: string; label: string; type?: ColType; }
export interface FilterDef {
  param: string;
  label: string;
  kind: 'text' | 'select' | 'date';
  options?: { value: string; label: string }[];
}

type Role = 'ADMINISTRADOR' | 'USUARIO' | 'COLABORADOR';

export interface ResourceConfig {
  path: string;
  label: string;
  icon: string;
  description: string;
  /** Perfis que veem a tela. Espelha as permissões do Back-End (docs/API.md). */
  roles: Role[];
  /** Endpoint por perfil (o primeiro perfil do usuário na ordem ADM > USU > COL define qual). */
  endpoint: Partial<Record<Role, string>>;
  columns: Column[];
  filters?: FilterDef[];
  /** A API devolve lista simples em vez de Pagina. */
  semPaginacao?: boolean;
  /** Mostra Aprovar/Reprovar nas solicitações pendentes. */
  analiseSolicitacao?: boolean;
}

const STATUS: FilterDef = {
  param: 'status', label: 'Situação do cadastro', kind: 'select',
  options: [{ value: 'ATIVO', label: 'Ativos' }, { value: 'INATIVO', label: 'Inativos' }]
};
const ADM: Role[] = ['ADMINISTRADOR'];
const ADM_USU: Role[] = ['ADMINISTRADOR', 'USUARIO'];
const TODOS: Role[] = ['ADMINISTRADOR', 'USUARIO', 'COLABORADOR'];

export const RESOURCES: ResourceConfig[] = [
  {
    path: 'pacientes', label: 'Pacientes', icon: '♙', roles: TODOS,
    description: 'Cadastro e consulta de pacientes e seus prontuários.',
    endpoint: { ADMINISTRADOR: '/pacientes', USUARIO: '/pacientes', COLABORADOR: '/pacientes' },
    filters: [{ param: 'nome', label: 'Buscar por nome', kind: 'text' }, STATUS],
    columns: [
      { key: 'nomeCompleto', label: 'Paciente' }, { key: 'cpf', label: 'CPF' },
      { key: 'dataNascimento', label: 'Nascimento', type: 'date' }, { key: 'telefone', label: 'Telefone' },
      { key: 'convenioNome', label: 'Convênio' }, { key: 'medicoNome', label: 'Médico' },
      { key: 'prontuarioNumero', label: 'Prontuário' }, { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'agendamentos', label: 'Agendamentos', icon: '▦', roles: ADM_USU,
    description: 'Consultas agendadas. Filtre por data para ver a agenda do dia.',
    endpoint: { ADMINISTRADOR: '/agendamentos', USUARIO: '/agendamentos' },
    filters: [{ param: 'data', label: 'Data da consulta', kind: 'date' }],
    columns: [
      { key: 'dataPrevista', label: 'Data', type: 'date' }, { key: 'horaPrevista', label: 'Hora', type: 'time' },
      { key: 'pacienteNome', label: 'Paciente' }, { key: 'medicoNome', label: 'Médico' },
      { key: 'situacao', label: 'Agendamento', type: 'badge' }, { key: 'dataRecepcao', label: 'Recepção', type: 'datetime' },
      { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'minha-agenda', label: 'Minha agenda', icon: '▦', roles: ['COLABORADOR'],
    description: 'Consultas agendadas para você.',
    endpoint: { COLABORADOR: '/atendimentos/agenda' }, semPaginacao: true,
    columns: [
      { key: 'dataPrevista', label: 'Data', type: 'date' }, { key: 'horaPrevista', label: 'Hora', type: 'time' },
      { key: 'pacienteNome', label: 'Paciente' }, { key: 'situacao', label: 'Agendamento', type: 'badge' },
      { key: 'dataRecepcao', label: 'Recepção', type: 'datetime' }
    ]
  },
  {
    path: 'atendimentos', label: 'Atendimentos', icon: '✚', roles: ['ADMINISTRADOR', 'COLABORADOR'],
    description: 'Registros de atendimento clínico (colaboradores veem apenas os seus).',
    endpoint: { ADMINISTRADOR: '/atendimentos', COLABORADOR: '/atendimentos/meus' },
    columns: [
      { key: 'dataHoraInicio', label: 'Início', type: 'datetime' }, { key: 'pacienteNome', label: 'Paciente' },
      { key: 'colaboradorNome', label: 'Profissional' }, { key: 'tipo', label: 'Tipo', type: 'badge' },
      { key: 'etapa', label: 'Etapa', type: 'badge' }, { key: 'diagnostico', label: 'Diagnóstico' },
      { key: 'internado', label: 'Internado', type: 'bool' }, { key: 'dataHoraEncerramento', label: 'Encerramento', type: 'datetime' }
    ]
  },
  {
    path: 'medicamentos', label: 'Medicamentos', icon: '⚕', roles: TODOS,
    description: 'Medicamentos cadastrados e saldo atual em estoque.',
    endpoint: { ADMINISTRADOR: '/medicamentos', USUARIO: '/medicamentos', COLABORADOR: '/medicamentos' },
    filters: [STATUS],
    columns: [
      { key: 'nome', label: 'Medicamento' }, { key: 'fabricante', label: 'Fabricante' },
      { key: 'armazenamento', label: 'Armazenamento', type: 'badge' }, { key: 'saldoAtual', label: 'Saldo', type: 'number' },
      { key: 'estoqueMinimo', label: 'Mínimo', type: 'number' }, { key: 'reposicaoNecessaria', label: 'Repor?', type: 'bool' },
      { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'reposicao', label: 'Reposição de estoque', icon: '⚠', roles: ADM_USU,
    description: 'Medicamentos com saldo igual ou abaixo do mínimo.',
    endpoint: { ADMINISTRADOR: '/estoque/reposicao', USUARIO: '/estoque/reposicao' }, semPaginacao: true,
    columns: [
      { key: 'nome', label: 'Medicamento' }, { key: 'fabricante', label: 'Fabricante' },
      { key: 'saldoAtual', label: 'Saldo atual', type: 'number' }, { key: 'estoqueMinimo', label: 'Mínimo', type: 'number' }
    ]
  },
  {
    path: 'movimentacoes', label: 'Movimentações', icon: '⇅', roles: ADM_USU,
    description: 'Entradas e saídas do estoque, com responsáveis e data/hora.',
    endpoint: { ADMINISTRADOR: '/estoque/movimentacoes', USUARIO: '/estoque/movimentacoes' },
    filters: [{
      param: 'tipo', label: 'Tipo', kind: 'select',
      options: [{ value: 'ENTRADA', label: 'Entradas' }, { value: 'SAIDA', label: 'Saídas' }]
    }, { param: 'inicio', label: 'De', kind: 'date' }, { param: 'fim', label: 'Até', kind: 'date' }],
    columns: [
      { key: 'dataHora', label: 'Data/hora', type: 'datetime' }, { key: 'medicamento', label: 'Medicamento' },
      { key: 'tipo', label: 'Tipo', type: 'badge' }, { key: 'quantidade', label: 'Qtd.', type: 'number' },
      { key: 'lote', label: 'Lote' }, { key: 'dataValidade', label: 'Validade', type: 'date' },
      { key: 'colaboradorSolicitante', label: 'Solicitante' }, { key: 'responsavelEntrega', label: 'Entregue por' }
    ]
  },
  {
    path: 'solicitacoes-medicamento', label: 'Solicitações de medicamento', icon: '✉', roles: TODOS,
    description: 'Pedidos de medicamento. Atendentes aprovam ou reprovam os pendentes.',
    endpoint: { ADMINISTRADOR: '/solicitacoes-medicamento', USUARIO: '/solicitacoes-medicamento', COLABORADOR: '/solicitacoes-medicamento/minhas' },
    analiseSolicitacao: true,
    filters: [{
      param: 'situacao', label: 'Situação', kind: 'select',
      options: [{ value: 'PENDENTE', label: 'Pendentes' }, { value: 'APROVADA', label: 'Aprovadas' },
        { value: 'REPROVADA', label: 'Reprovadas' }, { value: 'ENTREGUE', label: 'Entregues' }]
    }],
    columns: [
      { key: 'dataCadastro', label: 'Pedido em', type: 'datetime' }, { key: 'medicamento', label: 'Medicamento' },
      { key: 'quantidade', label: 'Qtd.', type: 'number' }, { key: 'colaboradorSolicitante', label: 'Solicitante' },
      { key: 'justificativa', label: 'Justificativa' }, { key: 'situacao', label: 'Situação', type: 'badge' },
      { key: 'analisadoPor', label: 'Analisado por' }
    ]
  },
  {
    path: 'colaboradores', label: 'Colaboradores', icon: '♧', roles: ADM,
    description: 'Profissionais e equipe cadastrados.',
    endpoint: { ADMINISTRADOR: '/colaboradores' }, filters: [STATUS],
    columns: [
      { key: 'nomeCompleto', label: 'Nome' }, { key: 'cpf', label: 'CPF' }, { key: 'funcao', label: 'Função', type: 'badge' },
      { key: 'especialidadeNome', label: 'Especialidade' }, { key: 'setorNome', label: 'Setor' },
      { key: 'email', label: 'E-mail' }, { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'convenios', label: 'Convênios', icon: '▤', roles: TODOS,
    description: 'Convênios aceitos pela clínica.',
    endpoint: { ADMINISTRADOR: '/convenios', USUARIO: '/convenios', COLABORADOR: '/convenios' }, filters: [STATUS],
    columns: [
      { key: 'nomeFantasia', label: 'Nome fantasia' }, { key: 'razaoSocial', label: 'Razão social' },
      { key: 'cnpj', label: 'CNPJ' }, { key: 'registroAns', label: 'Registro ANS' },
      { key: 'telefone', label: 'Telefone' }, { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'clinicas', label: 'Clínicas', icon: '⌂', roles: ADM,
    description: 'Dados administrativos das clínicas.',
    endpoint: { ADMINISTRADOR: '/clinicas' }, filters: [STATUS],
    columns: [
      { key: 'nomeFantasia', label: 'Nome fantasia' }, { key: 'razaoSocial', label: 'Razão social' },
      { key: 'cnpj', label: 'CNPJ' }, { key: 'cidade', label: 'Cidade' }, { key: 'estado', label: 'UF' },
      { key: 'diretorTecnicoNome', label: 'Diretor técnico' }, { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'setores', label: 'Setores', icon: '▥', roles: ADM,
    description: 'Setores vinculados às clínicas.',
    endpoint: { ADMINISTRADOR: '/setores' }, filters: [STATUS],
    columns: [
      { key: 'nome', label: 'Setor' }, { key: 'clinicaNome', label: 'Clínica' }, { key: 'responsavelNome', label: 'Responsável' },
      { key: 'email', label: 'E-mail' }, { key: 'telefone', label: 'Telefone' }, { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'usuarios', label: 'Usuários e perfis', icon: '☺', roles: ADM,
    description: 'Contas de acesso e perfis atribuídos.',
    endpoint: { ADMINISTRADOR: '/usuarios' }, filters: [STATUS],
    columns: [
      { key: 'login', label: 'Login' }, { key: 'colaboradorNome', label: 'Colaborador' },
      { key: 'perfis', label: 'Perfis', type: 'list' }, { key: 'ultimoLogin', label: 'Último acesso', type: 'datetime' },
      { key: 'status', label: 'Situação', type: 'badge' }
    ]
  },
  {
    path: 'auditoria', label: 'Auditoria', icon: '☑', roles: ADM,
    description: 'Rastro das operações realizadas no sistema.',
    endpoint: { ADMINISTRADOR: '/auditoria' },
    filters: [{ param: 'login', label: 'Usuário', kind: 'text' }, {
      param: 'acao', label: 'Ação', kind: 'select',
      options: ['LOGIN', 'LOGIN_FALHA', 'LOGOUT', 'ACESSO_NEGADO', 'ORIGEM_INVALIDA', 'CRIAR', 'EDITAR', 'INATIVAR', 'REATIVAR', 'MUDAR_STATUS', 'CONSULTAR', 'EMITIR_RELATORIO']
        .map((a) => ({ value: a, label: a.replace(/_/g, ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase()) }))
    }, { param: 'inicio', label: 'De', kind: 'date' }, { param: 'fim', label: 'Até', kind: 'date' }],
    columns: [
      { key: 'dataHora', label: 'Data/hora', type: 'datetime' }, { key: 'login', label: 'Usuário' },
      { key: 'acao', label: 'Ação', type: 'badge' }, { key: 'resultado', label: 'Resultado', type: 'badge' },
      { key: 'entidade', label: 'Entidade' }, { key: 'entidadeId', label: 'ID' },
      { key: 'descricao', label: 'Descrição' }, { key: 'ipOrigem', label: 'IP' }
    ]
  }
];

const ORDEM: Role[] = ['ADMINISTRADOR', 'USUARIO', 'COLABORADOR'];

/** Endpoint que vale para os perfis do usuário (ADM > USU > COL). */
export function endpointPara(cfg: ResourceConfig, perfis: readonly string[]): string | null {
  const perfil = ORDEM.find((p) => perfis.includes(p) && cfg.endpoint[p]);
  return perfil ? cfg.endpoint[perfil]! : null;
}
