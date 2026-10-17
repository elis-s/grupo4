import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ApiPage } from '../core/auth.models';
import { AuthService } from '../core/auth.service';
import { Column, ResourceConfig, RESOURCES, endpointPara } from '../core/resources';
import { formatarValor, mensagemDeErro, tom } from '../core/format';

type Linha = Record<string, unknown>;
const TAMANHO_PAGINA = 20;

/** Tela de consulta genérica: o que muda de módulo para módulo está em core/resources.ts. */
@Component({
  standalone: true,
  selector: 'app-resource-list',
  imports: [FormsModule],
  template: `
    <header class="page-head">
      <p class="eyebrow">CLÍNICA MÉDICA ACADÊMICA</p>
      <h1>{{ cfg.label }}</h1>
      <p class="muted">{{ cfg.description }}</p>
    </header>

    @if (cfg.filters?.length) {
      <form class="filters" (ngSubmit)="aplicarFiltros()">
        @for (f of cfg.filters; track f.param) {
          <label class="filter">
            <span>{{ f.label }}</span>
            @switch (f.kind) {
              @case ('select') {
                <select [name]="f.param" [(ngModel)]="filtros[f.param]">
                  <option value="">Todos</option>
                  @for (o of f.options; track o.value) { <option [value]="o.value">{{ o.label }}</option> }
                </select>
              }
              @case ('date') { <input type="date" [name]="f.param" [(ngModel)]="filtros[f.param]" /> }
              @default { <input type="search" [name]="f.param" [(ngModel)]="filtros[f.param]" placeholder="Digite para buscar" /> }
            }
          </label>
        }
        <div class="filter-actions">
          <button class="btn btn-primary" type="submit">Filtrar</button>
          <button class="btn btn-ghost" type="button" (click)="limparFiltros()">Limpar</button>
        </div>
      </form>
    }

    <section class="panel">
      <div class="panel-toolbar">
        <div><h2>Registros</h2><p>{{ totalItens() }} registro(s) encontrado(s)</p></div>
        <button class="btn btn-primary" type="button" (click)="carregar()" [disabled]="carregando()">
          {{ carregando() ? 'Carregando...' : '↻ Atualizar' }}
        </button>
      </div>

      @if (aviso()) { <div class="alert alert-ok" role="status">{{ aviso() }}</div> }
      @if (erro()) { <div class="alert alert-error" role="alert">{{ erro() }}</div> }

      @if (carregando() && !rows().length) {
        <div class="skeleton" aria-busy="true"><span></span><span></span><span></span><span></span></div>
      } @else if (!erro() && !rows().length) {
        <div class="empty-state"><span>▤</span><strong>Nenhum registro para exibir</strong>
          <p>Nada encontrado com os filtros atuais.</p></div>
      }

      @if (rows().length) {
        <div class="table-wrap" [class.loading]="carregando()">
          <table>
            <thead><tr>
              @for (c of cfg.columns; track c.key) { <th scope="col">{{ c.label }}</th> }
              @if (cfg.analiseSolicitacao && podeAnalisar) { <th scope="col">Ações</th> }
            </tr></thead>
            <tbody>
              @for (row of rows(); track $index) {
                <tr>
                  @for (c of cfg.columns; track c.key) {
                    <td [class.num]="c.type === 'number'" [title]="texto(row, c)">
                      @if (c.type === 'badge' && row[c.key]) {
                        <span class="badge tone-{{ tom(row[c.key]) }}">{{ texto(row, c) }}</span>
                      } @else if (c.key === 'reposicaoNecessaria') {
                        <span class="badge tone-{{ row[c.key] ? 'warn' : 'ok' }}">{{ row[c.key] ? 'Repor' : 'OK' }}</span>
                      } @else { {{ texto(row, c) }} }
                    </td>
                  }
                  @if (cfg.analiseSolicitacao && podeAnalisar) {
                    <td>
                      @if (row['situacao'] === 'PENDENTE') {
                        <div class="actions"><button class="btn btn-sm btn-primary" type="button" [disabled]="processando() === row['id']" (click)="aprovar(row)">Aprovar</button>
                        <button class="btn btn-sm btn-danger" type="button" [disabled]="processando() === row['id']" (click)="abrirReprovar(row)">Reprovar</button></div>
                      } @else { <span class="muted">—</span> }
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      @if (totalPaginas() > 1) {
        <div class="pagination">
          <button type="button" (click)="mudarPagina(pagina() - 1)" [disabled]="pagina() <= 0 || carregando()">Anterior</button>
          <span>Página {{ pagina() + 1 }} de {{ totalPaginas() }}</span>
          <button type="button" (click)="mudarPagina(pagina() + 1)" [disabled]="pagina() + 1 >= totalPaginas() || carregando()">Próxima</button>
        </div>
      }
    </section>

    @if (reprovando(); as sol) {
      <div class="modal-backdrop" (click)="fecharReprovar()">
        <div class="modal" role="dialog" aria-modal="true" aria-labelledby="titulo-reprovar" (click)="$event.stopPropagation()">
          <h2 id="titulo-reprovar">Reprovar solicitação</h2>
          <p class="muted">{{ sol['medicamento'] }} — {{ sol['quantidade'] }} un. ({{ sol['colaboradorSolicitante'] }})</p>
          <label class="filter"><span>Motivo da reprovação</span>
            <textarea rows="3" [(ngModel)]="motivo" name="motivo" placeholder="Explique o motivo"></textarea></label>
          @if (erroModal()) { <p class="alert alert-error" role="alert">{{ erroModal() }}</p> }
          <div class="modal-actions">
            <button class="btn btn-ghost" type="button" (click)="fecharReprovar()">Cancelar</button>
            <button class="btn btn-danger" type="button" [disabled]="!motivo.trim() || processando() !== null" (click)="confirmarReprovar(sol)">Confirmar reprovação</button>
          </div>
        </div>
      </div>
    }
  `
})
export class ResourceListComponent implements OnDestroy {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  cfg: ResourceConfig = RESOURCES.find((r) => r.path === inject(ActivatedRoute).snapshot.data['resource'])!;
  private endpoint = endpointPara(this.cfg, this.auth.perfis());
  /** Só atendente/administrador analisa solicitações (o back também confere). */
  podeAnalisar = this.auth.temAlgumPerfil(['ADMINISTRADOR', 'USUARIO']);

  filtros: Record<string, string> = this.filtrosVazios();
  motivo = '';

  rows = signal<Linha[]>([]);
  carregando = signal(false);
  erro = signal('');
  aviso = signal('');
  pagina = signal(0);
  totalPaginas = signal(1);
  totalItens = signal(0);
  processando = signal<unknown>(null);
  reprovando = signal<Linha | null>(null);
  erroModal = signal('');
  readonly tom = tom;

  private requisicao?: Subscription;

  constructor() { this.carregar(); }
  ngOnDestroy(): void { this.requisicao?.unsubscribe(); }

  texto(row: Linha, c: Column): string { return formatarValor(row[c.key], c.type); }

  carregar(): void {
    if (!this.endpoint) { this.erro.set('Seu perfil não tem acesso a este módulo.'); return; }
    this.requisicao?.unsubscribe(); // ignora resposta antiga se o usuário filtrar de novo
    this.carregando.set(true);
    this.erro.set('');

    let params = new HttpParams();
    if (!this.cfg.semPaginacao) params = params.set('page', this.pagina()).set('size', TAMANHO_PAGINA);
    for (const [chave, valor] of Object.entries(this.filtros)) if (valor?.trim()) params = params.set(chave, valor.trim());

    this.requisicao = this.http.get<ApiPage<Linha> | Linha[]>(`/api${this.endpoint}`, { params }).subscribe({
      next: (res) => {
        if (Array.isArray(res)) {
          this.rows.set(res); this.totalItens.set(res.length); this.totalPaginas.set(1);
        } else {
          this.rows.set(res.itens ?? []);
          this.totalItens.set(res.totalItens ?? res.itens?.length ?? 0);
          this.totalPaginas.set(Math.max(1, res.totalPaginas ?? 1));
        }
        this.carregando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.carregando.set(false);
        this.rows.set([]); this.totalItens.set(0);
        if (e.status !== 401) // 401: o interceptor já leva ao login
          this.erro.set(mensagemDeErro(e, 'Não foi possível carregar os registros. Tente novamente.'));
      }
    });
  }

  aplicarFiltros(): void { this.pagina.set(0); this.carregar(); }
  private filtrosVazios(): Record<string, string> {
    return Object.fromEntries((this.cfg.filters ?? []).map((f) => [f.param, '']));
  }
  limparFiltros(): void { this.filtros = this.filtrosVazios(); this.aplicarFiltros(); }
  mudarPagina(p: number): void {
    if (p >= 0 && p < this.totalPaginas()) { this.pagina.set(p); this.carregar(); }
  }

  // ---- análise de solicitações (RF059) ----
  aprovar(row: Linha): void { this.analisar(row, 'aprovar', {}, 'Solicitação aprovada.'); }

  abrirReprovar(row: Linha): void { this.motivo = ''; this.erroModal.set(''); this.reprovando.set(row); }
  fecharReprovar(): void { this.reprovando.set(null); }
  confirmarReprovar(row: Linha): void { this.analisar(row, 'reprovar', { motivo: this.motivo.trim() }, 'Solicitação reprovada.'); }

  private analisar(row: Linha, acao: 'aprovar' | 'reprovar', corpo: object, sucesso: string): void {
    this.processando.set(row['id']);
    this.aviso.set(''); this.erro.set(''); this.erroModal.set('');
    this.http.patch(`/api/solicitacoes-medicamento/${row['id']}/${acao}`, corpo).subscribe({
      next: () => { this.processando.set(null); this.reprovando.set(null); this.aviso.set(sucesso); this.carregar(); },
      error: (e: HttpErrorResponse) => {
        this.processando.set(null);
        const msg = mensagemDeErro(e, 'Não foi possível concluir a operação.');
        if (this.reprovando()) this.erroModal.set(msg); else this.erro.set(msg);
      }
    });
  }
}
