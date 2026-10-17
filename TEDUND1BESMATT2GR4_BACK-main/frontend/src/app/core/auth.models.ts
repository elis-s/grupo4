export type Perfil = 'ADMINISTRADOR' | 'COLABORADOR' | 'USUARIO';

export interface LoggedUser {
  id: number;
  login: string;
  nome: string;
  perfis: string[];
  colaboradorId: number | null;
  funcao: string | null;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEm: string;
  usuario: LoggedUser;
}

export interface ApiPage<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

/** Formato do erro padrão da API (seção 4.2.2 do documento). */
export interface ApiError {
  status: number;
  erro: string;
  mensagem?: string;
  campos?: Record<string, string>;
}
