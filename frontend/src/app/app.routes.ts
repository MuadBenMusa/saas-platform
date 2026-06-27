import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LandingPageComponent } from './features/landing/landing-page.component';
import { LoginPageComponent } from './features/login/login-page.component';
import { AppShellComponent } from './layout/app-shell/app-shell.component';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { ProjectsPageComponent } from './features/projects/pages/projects-page.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: LandingPageComponent
  },
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'dashboard',
        component: DashboardPageComponent
      },
      {
        path: 'projects',
        component: ProjectsPageComponent
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
