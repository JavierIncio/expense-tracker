import { inject } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { map, Observable, of } from 'rxjs';

export const authGuard = (): Observable<boolean | UrlTree> => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated()) return of(true);

  return auth.initialize().pipe(
    map(() => auth.isAuthenticated() ? true : router.createUrlTree(['/login'])));
};
