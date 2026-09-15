import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { LoginRequest, RegisterRequest, TokenResponse } from '@core/models/auth.models';
import { decodeJwt, isTokenExpired } from '@core/utils/jwt';
import { environment } from '@env/environment';
import { catchError, finalize, Observable, of, shareReplay, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly accessTokenStore = signal<string | null>(null);
  private readonly isInitializingStore = signal<boolean>(true);
  private refreshInFlight: Observable<TokenResponse | null> | null = null;
  private sessionResolved = false;

  readonly accessToken = this.accessTokenStore.asReadonly();
  readonly isInitializing = this.isInitializingStore.asReadonly();

  readonly currentUser = computed(() => {
    const token = this.accessToken();
    return token ? decodeJwt(token) : null;
  });

  readonly isAuthenticated = computed(() => {
    const payload = this.currentUser();
    return !!payload && !isTokenExpired(payload);
  });

  register(payload: RegisterRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/register`, payload)
      .pipe(tap((tokens) => this.accessTokenStore.set(tokens.accessToken)));
  }

  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(tap((tokens) => this.accessTokenStore.set(tokens.accessToken)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${environment.apiUrl}/auth/logout`, null)
      .pipe(finalize(() => this.clearSession()));
  }

  refresh(): Observable<TokenResponse | null> {
    if (this.refreshInFlight) return this.refreshInFlight;

    this.refreshInFlight = this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/refresh`, null)
      .pipe(
        tap((tokens) => this.accessTokenStore.set(tokens.accessToken)),
        finalize(() => (this.refreshInFlight = null)),
        shareReplay(1),
      );

    return this.refreshInFlight;
  }

  clearSession(): void {
    this.accessTokenStore.set(null);
  }

  initialize(): Observable<TokenResponse | null> {
    this.isInitializingStore.set(true);
    return (this.isAuthenticated() || this.sessionResolved ? of(null) : this.refresh()).pipe(
      catchError(() => of(null)),
      finalize(() => {
        this.sessionResolved = true;
        this.isInitializingStore.set(false);
      }),
    );
  }
}
