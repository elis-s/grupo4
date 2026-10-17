import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ApiPage } from '../core/auth.models';
import { RESOURCES } from '../core/resources';

interface Indicador { rotulo: string; link: string; valor: number | null; alerta?: boolean; }

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <header class="page-head">
      <p class="eyebrow">PAINEL DE CONTROLE</p>
      <h1>Olá, {{ primeiroNome() }}!</h1>
    </header>

    <section class="welcome-panel">
      <div>
        <p class="welcome-label">CLÍNICA MÉDICA ACADÊMICA</p>
        <h2>Gestão integrada, cuidado organizado.</h2>
        <p>Acesse os módulos pelos atalhos abaixo ou pelo menu lateral.</p>
      </div>
      <div class="welcome-symbol" aria-hidden="true">✚</div>
    </section>

    @if (indicadores().length) {
      <section class="stat-grid" aria-label="Indicadores">
        @for (i of indicadores(); track i.link) {
          <a class="stat-card" [class.stat-alert]="i.alerta && (i.valor ?? 0) > 0" [routerLink]="i.link">
            <span class="stat-value">{{ i.valor ?? '—' }}</span>
            <span class="stat-label">{{ i.rotulo }}</span>
          </a>
        }
      </section>
    }

    <section class="section-heading">
      <h2>Módulos do sistema</h2>
      <p>Os recursos exibidos dependem das permissões da sua conta.</p>
    </section>
    <section class="module-grid">
      @for (m of modulos(); track m.path) {
        <a class="module-card" [routerLink]="'/' + m.path">
          <span class="module-icon">{{ m.icon }}</span>
          <span class="module-title">{{ m.label }}</span>
          <span class="module-desc">{{ m.description }}</span>
          <span class="module-arrow">Acessar <b>→</b></span>
        </a>
      }
    </section>
  `
})
export class DashboardComponent {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  modulos = computed(() => RESOURCES.filter((r) => this.auth.temAlgumPerfil(r.roles)));
  primeiroNome = computed(() => (this.auth.usuario()?.nome || this.auth.usuario()?.login || 'usuário').split(' ')[0]);
  indicadores = signal<Indicador[]>([]);

  constructor() {
    const perfis = this.auth.perfis();
    const lista: Indicador[] = [];
    if (perfis.includes('ADMINISTRADOR') || perfis.includes('USUARIO')) {
      lista.push(this.ind('Medicamentos para repor', '/reposicao', true,
        this.http.get<unknown[]>('/api/estoque/reposicao'), (r) => r.length));
      lista.push(this.ind('Solicitações pendentes', '/solicitacoes-medicamento', true,
        this.http.get<ApiPage<unknown>>('/api/solicitacoes-medicamento', { params: { situacao: 'PENDENTE', size: 1 } }), (r) => r.totalItens));
    } else if (perfis.includes('COLABORADOR')) {
      lista.push(this.ind('Consultas na minha agenda', '/minha-agenda', false,
        this.http.get<unknown[]>('/api/atendimentos/agenda'), (r) => r.length));
      lista.push(this.ind('Minhas solicitações', '/solicitacoes-medicamento', false,
        this.http.get<ApiPage<unknown>>('/api/solicitacoes-medicamento/minhas', { params: { size: 1 } }), (r) => r.totalItens));
    }
    this.indicadores.set(lista);
  }

  /** Cria o indicador já "vazio" e o preenche quando a API responder (erro = fica em "—"). */
  private ind<T>(rotulo: string, link: string, alerta: boolean, req: import('rxjs').Observable<T>, extrair: (r: T) => number): Indicador {
    const item: Indicador = { rotulo, link, valor: null, alerta };
    req.subscribe({
      next: (r) => this.indicadores.update((l) => l.map((x) => (x.link === link && x.rotulo === rotulo ? { ...x, valor: extrair(r) } : x))),
      error: () => undefined
    });
    return item;
  }
}
