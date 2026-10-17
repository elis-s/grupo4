import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth.guard';
import { RESOURCES } from './core/resources';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login.component').then((m) => m.LoginComponent) },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./pages/dashboard.component').then((m) => m.DashboardComponent) },
      // Uma rota por tela do catálogo (core/resources.ts), já protegida pelo perfil.
      ...RESOURCES.map((r) => ({
        path: r.path,
        canActivate: [roleGuard],
        data: { roles: r.roles, resource: r.path },
        loadComponent: () => import('./pages/resource-list.component').then((m) => m.ResourceListComponent)
      })),
      { path: '', pathMatch: 'full' as const, redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
