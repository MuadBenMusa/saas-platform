import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { firstValueFrom, of, Observable } from 'rxjs';
import { CurrentSession } from './session.model';
import { SessionService } from './session.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  const session: CurrentSession = {
    user: {
      id: 'user-id',
      email: 'owner@example.test',
      name: 'Demo Owner',
      status: 'ACTIVE'
    },
    activeTenant: {
      id: 'tenant-id',
      name: 'Demo Company',
      slug: 'demo-company',
      role: 'OWNER'
    }
  };

  it('allows an authenticated SaaS session', async () => {
    configure(of(session));

    const result = TestBed.runInInjectionContext(() => authGuard(
      {} as never,
      {} as never
    ));

    await expect(firstValueFrom(result as Observable<boolean | UrlTree>))
      .resolves.toBe(true);
  });

  it('redirects an unauthenticated user to login', async () => {
    const loginTree = {} as UrlTree;
    const createUrlTree = configure(of(null), loginTree);

    const result = TestBed.runInInjectionContext(() => authGuard(
      {} as never,
      {} as never
    ));

    await expect(firstValueFrom(result as Observable<boolean | UrlTree>))
      .resolves.toBe(loginTree);
    expect(createUrlTree).toHaveBeenCalledWith(['/login']);
  });

  function configure(
    sessionResult: Observable<CurrentSession | null>,
    loginTree = {} as UrlTree
  ) {
    const loadSession = vi.fn(() => sessionResult);
    const createUrlTree = vi.fn(() => loginTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: SessionService, useValue: { loadSession } },
        { provide: Router, useValue: { createUrlTree } }
      ]
    });

    return createUrlTree;
  }
});
