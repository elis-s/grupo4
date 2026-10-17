import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoggedUser, LoginResponse } from './auth.models';

const TOKEN_KEY = 'clinica_token';
const USER_KEY = 'clinica_usuario';
const EXPIRA_KEY = 'clinica_expira_em';
export const AVISO_KEY = 'clinica_aviso';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private _usuario = signal<LoggedUser | null>(this.lerUsuario());
  readonly usuario = this._usuario.asReadonly();
  readonly perfis = computed(() => this._usuario()?.perfis ?? []);

  login(login: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { login, senha }).pipe(
      tap((r) => {
        sessionStorage.setItem(TOKEN_KEY, r.token);
        sessionStorage.setItem(USER_KEY, JSON.stringify(r.usuario));
        sessionStorage.setItem(EXPIRA_KEY, r.expiraEm);
        sessionStorage.removeItem(AVISO_KEY);
        this._usuario.set(r.usuario);
      })
    );
  }

  token(): string | null { return sessionStorage.getItem(TOKEN_KEY); }

  /** Há token e ele ainda não expirou (o back também valida: isto só evita chamadas inúteis). */
  estaAutenticado(): boolean {
    if (!this.token()) return false;
    const expira = sessionStorage.getItem(EXPIRA_KEY);
    if (expira && Date.parse(expira) <= Date.now()) { this.limparSessao(); return false; }
    return true;
  }

  temAlgumPerfil(exigidos: readonly string[] | undefined): boolean {
    if (!exigidos || exigidos.length === 0) return true;
    return exigidos.some((p) => this.perfis().includes(p));
  }

  /** Logout voluntário: avisa o back (que revoga o token) e volta ao login. */
  logout(): void {
    if (!this.token()) { this.irParaLogin(); return; }
    this.http.post<void>('/api/auth/logout', {}).subscribe({
      next: () => this.irParaLogin(),
      error: () => this.irParaLogin()
    });
  }

  /** Chamado pelo interceptor quando a API responde 401 (token vencido/revogado). */
  sessaoExpirada(): void {
    if (!this.token()) return;
    this.irParaLogin('Sua sessão expirou. Entre novamente.');
  }

  private irParaLogin(aviso?: string): void {
    this.limparSessao();
    if (aviso) sessionStorage.setItem(AVISO_KEY, aviso);
    void this.router.navigate(['/login']);
  }

  private limparSessao(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(EXPIRA_KEY);
    this._usuario.set(null);
  }

  private lerUsuario(): LoggedUser | null {
    const v = sessionStorage.getItem(USER_KEY);
    if (!v) return null;
    try { return JSON.parse(v) as LoggedUser; } catch { return null; }
  }
}
