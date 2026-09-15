import { Routes } from '@angular/router';
import { authRoutes } from '@features/auth/auth.routes';
import { mainRoutes } from '@features/main/main.routes';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  ...authRoutes,
  ...mainRoutes,
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
