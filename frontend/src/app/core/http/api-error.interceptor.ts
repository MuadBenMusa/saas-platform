import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from '../auth/session.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const sessionService = inject(SessionService);

  return next(request).pipe(
    catchError(error => {
      const requestPath = getPath(request.url);
      const isApiRequest = requestPath.startsWith('/api/');
      const isSessionProbe = requestPath === '/api/users/me';
      const isUnauthorized = error.status === 401;

      if (isUnauthorized && isApiRequest) {
        sessionService.clearCachedSession();

        if (!isSessionProbe && !router.url.startsWith('/login')) {
          void router.navigate(['/login']);
        }
      }

      if (!isSessionProbe && !isUnauthorized) {
        console.error('API error', error);
      }

      return throwError(() => error);
    })
  );
};

function getPath(url: string): string {
  try {
    return new URL(url, window.location.origin).pathname;
  } catch {
    return url;
  }
}
