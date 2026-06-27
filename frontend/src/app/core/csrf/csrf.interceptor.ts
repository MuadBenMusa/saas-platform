import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { CsrfService } from './csrf.service';

// Because the BFF uses cookie-based sessions instead of passing tokens in headers,
// explicit CSRF protection is required to prevent cross-site request forgery.
const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  if (SAFE_METHODS.includes(request.method.toUpperCase())) {
    return next(request);
  }

  const csrfService = inject(CsrfService);

  return csrfService.getToken().pipe(
    switchMap(csrf => {
      const requestWithCsrf = request.clone({
        setHeaders: {
          [csrf.headerName]: csrf.token
        }
      });

      return next(requestWithCsrf);
    })
  );
};
