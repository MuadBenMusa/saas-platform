import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const sessionService = inject(SessionService);

  return sessionService.loadSession().pipe(
    map(session => session !== null
      ? true
      : router.createUrlTree(['/login'])
    )
  );
};
