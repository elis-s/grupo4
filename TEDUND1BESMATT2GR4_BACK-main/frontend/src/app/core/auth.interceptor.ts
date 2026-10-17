import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/** Anexa o JWT e, se a API devolver 401 fora do login, encerra a sessão. */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const ehLogin = request.url.endsWith('/api/auth/login');
  const token = auth.token();
  const req = token && !ehLogin ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401 && !ehLogin && !request.url.endsWith('/api/auth/logout')) auth.sessaoExpirada();
      return throwError(() => e);
    })
  );
};
