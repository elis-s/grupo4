import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaAutenticado() ? true : inject(Router).createUrlTree(['/login']);
};

/** Bloqueia a rota se o perfil não estiver em `data.roles`. É só conveniência de UX: quem manda é o Back-End. */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  return auth.temAlgumPerfil(route.data['roles'] as string[] | undefined)
    ? true
    : inject(Router).createUrlTree(['/dashboard']);
};
