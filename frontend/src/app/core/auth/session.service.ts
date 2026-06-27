import { HttpClient } from '@angular/common/http';
import { computed, Injectable, signal } from '@angular/core';
import { catchError, finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { CsrfService } from '../csrf/csrf.service';
import { CurrentSession } from './session.model';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private readonly loginUrl =
    '/oauth2/authorization/oauth2-authorization-flow';

  // Angular does not manage, store, or decode JWTs. The browser only carries the session cookie;
  // OAuth2 tokens stay server-side in the Spring BFF to reduce token exposure in the frontend.
  private sessionRequest?: Observable<CurrentSession | null>;

  readonly session = signal<CurrentSession | null>(null);
  readonly loading = signal(false);
  readonly initialized = signal(false);
  readonly loggingOut = signal(false);
  readonly error = signal<string | null>(null);

  readonly isAuthenticated = computed(() => this.session() !== null);

  constructor(
    private readonly http: HttpClient,
    private readonly csrfService: CsrfService
  ) {}

  loadSession(force = false): Observable<CurrentSession | null> {
    if (this.initialized() && !force) {
      return of(this.session());
    }

    if (this.sessionRequest) {
      return this.sessionRequest;
    }

    this.loading.set(true);
    this.error.set(null);

    this.sessionRequest = this.http.get<CurrentSession>('/api/users/me', {
      redirect: 'manual'
    }).pipe(
      tap(session => {
        this.session.set(session);
      }),
      catchError(() => {
        this.session.set(null);
        return of(null);
      }),
      finalize(() => {
        this.loading.set(false);
        this.initialized.set(true);
        this.sessionRequest = undefined;
      }),
      shareReplay(1)
    );

    return this.sessionRequest;
  }

  login(): void {
    window.location.href = this.loginUrl;
  }

  clearCachedSession(): void {
    this.session.set(null);
    this.initialized.set(true);
    this.sessionRequest = undefined;
    this.csrfService.clear();
  }

  logout(): void {
    if (this.loggingOut()) {
      return;
    }

    this.loggingOut.set(true);
    this.error.set(null);
    this.csrfService.clear();

    this.csrfService.getToken().subscribe({
      next: csrf => {
        this.csrfService.clear();

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';

        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrf.parameterName;
        csrfInput.value = csrf.token;

        form.appendChild(csrfInput);
        document.body.appendChild(form);
        form.submit();
      },
      error: () => {
        this.csrfService.clear();
        this.error.set('Could not start logout.');
        this.loggingOut.set(false);
      }
    });
  }
}
