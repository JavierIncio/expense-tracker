import { Routes } from '@angular/router';
import { guestGuard } from '@core/guards/guest.guard';

export const authRoutes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./register/register').then((m) => m.Register),
  },
];
