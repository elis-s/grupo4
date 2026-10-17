export type ColType = 'text' | 'date' | 'datetime' | 'time' | 'number' | 'badge' | 'bool' | 'list';

const TONES: Record<string, string> = {
  ATIVO: 'ok', APROVADA: 'ok', ENTREGUE: 'ok', FINALIZADO: 'ok', ENTRADA: 'ok', SUCESSO: 'ok',
  PENDENTE: 'warn', AGENDADO: 'warn', EM_TRIAGEM: 'warn', EM_ATENDIMENTO: 'info', SAIDA: 'info',
  REPROVADA: 'bad', INATIVO: 'bad', FALHA: 'bad', NEGADO: 'bad', URGENCIA: 'warn', EMERGENCIA: 'bad'
};

export function tom(valor: unknown): string { return TONES[String(valor)] ?? 'neutral'; }

/** 'EM_TRIAGEM' -> 'Em triagem' */
export function humanizar(valor: unknown): string {
  const s = String(valor).replace(/_/g, ' ').toLowerCase();
  return s.charAt(0).toUpperCase() + s.slice(1);
}

/**
 * LocalDate chega como 'AAAA-MM-DD'. Não usar `new Date('2026-10-16')`: o JS lê como UTC
 * e, no fuso do Brasil, mostraria o dia anterior. Por isso a data é quebrada na mão.
 */
export function formatarData(v: string): string {
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(v);
  return m ? `${m[3]}/${m[2]}/${m[1]}` : v;
}

export function formatarDataHora(v: string): string {
  const d = new Date(v);
  if (isNaN(d.getTime())) return v;
  return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

export function formatarValor(valor: unknown, tipo: ColType = 'text'): string {
  if (valor === null || valor === undefined || valor === '') return '—';
  switch (tipo) {
    case 'date': return formatarData(String(valor));
    case 'datetime': return formatarDataHora(String(valor));
    case 'time': return String(valor).slice(0, 5);
    case 'bool': return valor ? 'Sim' : 'Não';
    case 'number': return Number(valor).toLocaleString('pt-BR');
    case 'badge': return humanizar(valor);
    case 'list': return Array.isArray(valor) ? valor.map(humanizar).join(', ') : String(valor);
    default: return String(valor);
  }
}

/** Extrai a mensagem do erro padrão da API (campo `mensagem` ou `campos`). */
export function mensagemDeErro(e: { status?: number; error?: unknown }, padrao: string): string {
  const corpo = e.error as { mensagem?: string; campos?: Record<string, string> } | null;
  if (corpo?.mensagem) return corpo.mensagem;
  if (corpo?.campos) return Object.values(corpo.campos).join(' ');
  if (e.status === 403) return 'Seu perfil não tem permissão para esta operação.';
  if (e.status === 0) return 'Sem conexão com o servidor. Confira se o Back-End está rodando e se o proxy está configurado.';
  return padrao;
}
