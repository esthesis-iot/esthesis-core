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
  {path: 'dashboard',     loadChildren: './dashboard/dashboard.module#DashboardModule', canActivate: [CanActivateGuard]},
  {path: 'audit',         loadChildren: './audit/audit.module#AuditModule', canActivate: [CanActivateGuard]},
  {path: 'devices',       loadChildren: './devices/devices.module#DevicesModule', canActivate: [CanActivateGuard]},
  {path: 'cas',           loadChildren: './cas/cas.module#CasModule', canActivate: [CanActivateGuard]},
  {path: 'certificates',  loadChildren: './certificates/certificates.module#CertificatesModule', canActivate: [CanActivateGuard]},
  {path: 'infra',         loadChildren: './infrastructure/infrastructure.module#InfrastructureModule', canActivate: [CanActivateGuard]},
  {path: 'provisioning',  loadChildren: './provisioning/provisioning.module#ProvisioningModule', canActivate: [CanActivateGuard]},
  {path: 'users',         loadChildren: './users/users.module#UsersModule', canActivate: [CanActivateGuard]},
  {path: 'settings',      loadChildren: './settings/settings.module#SettingsModule', canActivate: [CanActivateGuard]},
  {path: 'tags',          loadChildren: './tags/tags.module#TagsModule', canActivate: [CanActivateGuard]},
  {path: 'applications',  loadChildren: './applications/applications.module#ApplicationsModule', canActivate: [CanActivateGuard]},
  {path: 'datasinks',     loadChildren: './datasinks/datasinks.module#DatasinksModule', canActivate: [CanActivateGuard]},
  {path: 'control',       loadChildren: './control/control.module#ControlModule', canActivate: [CanActivateGuard]},
  {path: 'about',         loadChildren: './about/about.module#AboutModule', canActivate: [CanActivateGuard]},

  // Redirect all other urls to Dashboard.
  {path: '**', redirectTo: 'dashboard'}
],
{
  enableTracing: false
});
