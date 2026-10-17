import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { RESOURCES } from '../core/resources';
import { humanizar } from '../core/format';

@Component({
  standalone: true,
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <header class="mobile-bar">
        <button class="icon-btn" type="button" (click)="menuAberto.set(!menuAberto())" aria-label="Abrir menu">☰</button>
        <strong>Clínica Médica Acadêmica</strong>
      </header>

      <aside class="sidebar" [class.open]="menuAberto()">
        <a class="brand" routerLink="/dashboard"><span class="brand-icon">C</span>
          <span>Clínica Médica<small>Acadêmica</small></span></a>
        <p class="menu-caption">MENU</p>
        <nav aria-label="Menu principal">
          <a routerLink="/dashboard" routerLinkActive="active"><span class="nav-icon">⌂</span>Visão geral</a>
          @for (item of menu(); track item.path) {
            <a [routerLink]="'/' + item.path" routerLinkActive="active"><span class="nav-icon">{{ item.icon }}</span>{{ item.label }}</a>
          }
        </nav>
        <div class="sidebar-bottom">
          <div class="avatar">{{ iniciais() }}</div>
          <div class="user-mini"><strong>{{ nome() }}</strong><small>{{ perfisTexto() }}</small></div>
          <button class="icon-btn" type="button" title="Sair" aria-label="Sair" (click)="sair()">↪</button>
        </div>
      </aside>
      @if (menuAberto()) { <div class="backdrop" (click)="menuAberto.set(false)"></div> }

      <main class="main-area"><router-outlet /></main>
    </div>
  `
})
export class ShellComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  menuAberto = signal(false);

  menu = computed(() => RESOURCES.filter((r) => this.auth.temAlgumPerfil(r.roles)));
  nome = computed(() => this.auth.usuario()?.nome || this.auth.usuario()?.login || 'Usuário');
  perfisTexto = computed(() => this.auth.perfis().map(humanizar).join(' • '));
  iniciais = computed(() => this.nome().split(' ').filter(Boolean).slice(0, 2).map((x) => x[0]).join('').toUpperCase());

  constructor() {
    // fecha o menu no celular ao navegar
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => this.menuAberto.set(false));
  }

  sair(): void { this.auth.logout(); }
}
