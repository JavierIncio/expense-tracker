import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { MainShell } from './main-shell/main-shell';

export const mainRoutes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    component: MainShell,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'transactions',
        loadComponent: () => import('./transactions/transactions').then((m) => m.Transactions),
      },
      {
        path: 'categories',
        loadComponent: () => import('./categories/categories').then((m) => m.Categories),
      },
      {
        path: 'budgets',
        loadComponent: () => import('./budgets/budgets').then((m) => m.Budgets),
      },
    ],
  },
];
