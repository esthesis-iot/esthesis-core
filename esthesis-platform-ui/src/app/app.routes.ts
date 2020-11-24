import {RouterModule} from '@angular/router';
import {ModuleWithProviders} from '@angular/core';
import {LoginComponent} from './auth/login.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {LogoutComponent} from './auth/logout.component';
import {ForgotPasswordComponent} from './auth/forgot-password.component';

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
    {
      path: 'dashboard',
      loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule)
    },
    {
      path: 'audit',
      loadChildren: () => import('./audit/audit.module').then(m => m.AuditModule)
    },
    {
      path: 'devices',
      loadChildren: () => import('./devices/devices.module').then(m => m.DevicesModule)
    },
    {
      path: 'cas',
      loadChildren: () => import('./cas/cas.module').then(m => m.CasModule)
    },
    {
      path: 'certificates',
      loadChildren: () => import('./certificates/certificates.module').then(
        m => m.CertificatesModule)
    },
    {
      path: 'stores',
      loadChildren: () => import('./stores/stores.module').then(m => m.StoresModule)
    },
    {
      path: 'infra',
      loadChildren: () => import('./infrastructure/infrastructure.module').then(
        m => m.InfrastructureModule)
    },
    {
      path: 'provisioning',
      loadChildren: () => import('./provisioning/provisioning.module').then(
        m => m.ProvisioningModule)
    },
    {
      path: 'users',
      loadChildren: () => import('./users/users.module').then(m => m.UsersModule)
    },
    {
      path: 'settings',
      loadChildren: () => import('./settings/settings.module').then(m => m.SettingsModule)
    },
    {
      path: 'tags',
      loadChildren: () => import('./tags/tags.module').then(m => m.TagsModule)
    },
    {
      path: 'applications',
      loadChildren: () => import('./applications/applications.module').then(
        m => m.ApplicationsModule)
    },
    {
      path: 'readers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule)
    },
    {
      path: 'producers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule)
    },
    {
      path: 'writers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule)
    },
    {
      path: 'loggers',
      loadChildren: () => import('./nifisinks/nifisink.module').then(m => m.NifisinkModule)
    },
    {
      path: 'command',
      loadChildren: () => import('./command/command.module').then(m => m.CommandModule)
    },
    {
      path: 'about',
      loadChildren: () => import('./about/about.module').then(m => m.AboutModule)
    },
    {
      path: 'data-wizards',
      loadChildren: () => import('./datawizards/datawizards.module').then(m => m.DatawizardsModule)
    },

    // Redirect all other urls to Dashboard.
    {path: '**', redirectTo: 'dashboard'}
  ],
  {
    enableTracing: false,
    scrollPositionRestoration: 'enabled'
  });
