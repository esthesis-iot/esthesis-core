import {RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {LoginComponent} from './auth/login.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {LogoutComponent} from './auth/logout.component';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {CanActivateGuard} from './shared/guards/can-activate-guard';

// Configuration of Router with all available AppConstants.Routes.
export const routing: ModuleWithProviders = RouterModule.forRoot([
  // Redirect for empty path.
  {path: '', redirectTo: 'dashboard', pathMatch: 'full'},

  // Generic routes.
  {path: 'login', component: LoginComponent},
  {path: 'new-password', component: NewPasswordComponent},
  {path: 'logout', component: LogoutComponent},
  {path: 'forgot-password', component: ForgotPasswordComponent},

  // Features routes.
  {path: 'dashboard',     loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule), canActivate: [CanActivateGuard]},
  {path: 'audit',         loadChildren: () => import('./audit/audit.module').then(m => m.AuditModule), canActivate: [CanActivateGuard]},
    {
      path: 'devices',
      loadChildren: () => import('./devices/devices.module').then(m => m.DevicesModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'cas',
      loadChildren: () => import('./cas/cas.module').then(m => m.CasModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'certificates',
      loadChildren: () => import('./certificates/certificates.module').then(
        m => m.CertificatesModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'stores',
      loadChildren: () => import('./stores/stores.module').then(m => m.StoresModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'infra',
      loadChildren: () => import('./infrastructure/infrastructure.module').then(
        m => m.InfrastructureModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'provisioning',
      loadChildren: () => import('./provisioning/provisioning.module').then(
        m => m.ProvisioningModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'users',
      loadChildren: () => import('./users/users.module').then(m => m.UsersModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'settings',
      loadChildren: () => import('./settings/settings.module').then(m => m.SettingsModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'tags',
      loadChildren: () => import('./tags/tags.module').then(m => m.TagsModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'applications',
      loadChildren: () => import('./applications/applications.module').then(
        m => m.ApplicationsModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'readers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'producers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'writers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'loggers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'control',
      loadChildren: () => import('./control/control.module').then(m => m.ControlModule),
      canActivate: [CanActivateGuard]
    },
    {
      path: 'about',
      loadChildren: () => import('./about/about.module').then(m => m.AboutModule),
      canActivate: [CanActivateGuard]
    },

    // Redirect all other urls to Dashboard.
    {path: '**', redirectTo: 'dashboard'}
  ],
{
  enableTracing: false
});
