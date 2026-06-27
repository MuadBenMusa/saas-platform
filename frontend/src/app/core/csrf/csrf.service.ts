import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { CsrfTokenResponse } from './csrf.model';

@Injectable({
  providedIn: 'root'
})
export class CsrfService {
  private loadingRequest?: Observable<CsrfTokenResponse>;

  readonly token = signal<CsrfTokenResponse | null>(null);

  constructor(private readonly http: HttpClient) {}

  getToken(): Observable<CsrfTokenResponse> {
    const existing = this.token();

    if (existing) {
      return of(existing);
    }

    if (!this.loadingRequest) {
      this.loadingRequest = this.http.get<CsrfTokenResponse>('/api/csrf').pipe(
        tap(token => this.token.set(token)),
        finalize(() => {
          this.loadingRequest = undefined;
        }),
        shareReplay(1)
      );
    }

    return this.loadingRequest;
  }

  clear(): void {
    this.token.set(null);
  }
}
