import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AVISO_KEY, AuthService } from '../core/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <main class="login-page">
      <section class="login-card">
        <div class="brand-mark">CMA</div>
        <p class="eyebrow">SISTEMA DE GESTÃO</p>
        <h1>Clínica Médica Acadêmica</h1>
        <p class="muted">Entre com seu usuário para continuar.</p>
        <form [formGroup]="form" (ngSubmit)="entrar()">
          <label for="login">Usuário</label>
          <input id="login" type="text" formControlName="login" autocomplete="username" placeholder="Seu usuário" />
          <label for="senha">Senha</label>
          <input id="senha" type="password" formControlName="senha" autocomplete="current-password" placeholder="Sua senha" />
          @if (aviso()) { <p class="alert alert-warn" role="status">{{ aviso() }}</p> }
          @if (erro()) { <p class="alert alert-error" role="alert">{{ erro() }}</p> }
          <button class="btn btn-primary full" type="submit" [disabled]="form.invalid || carregando()">
            {{ carregando() ? 'Entrando...' : 'Entrar no sistema' }}
          </button>
        </form>
        <p class="login-footnote">Acesso restrito a usuários autorizados.</p>
      </section>
    </main>
  `
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  form = inject(FormBuilder).nonNullable.group({
    login: ['', Validators.required],
    senha: ['', Validators.required]
  });
  carregando = signal(false);
  erro = signal('');
  aviso = signal(sessionStorage.getItem(AVISO_KEY) ?? '');

  constructor() {
    if (this.auth.estaAutenticado()) void this.router.navigate(['/dashboard']);
  }

  entrar(): void {
    if (this.form.invalid || this.carregando()) return;
    this.carregando.set(true);
    this.erro.set('');
    this.aviso.set('');
    const { login, senha } = this.form.getRawValue();
    this.auth.login(login.trim(), senha).subscribe({
      next: () => { this.carregando.set(false); void this.router.navigate(['/dashboard']); },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set(
          e.status === 401 ? 'Usuário ou senha inválidos.'
          : e.status === 403 ? 'Acesso recusado pelo Back-End. Confira se a X-Frontend-Key do proxy é igual à FRONTEND_KEY do local.properties.'
          : 'Não foi possível acessar o servidor. Confira se o Back-End está rodando e se o proxy está configurado.'
        );
      }
    });
  }
}
