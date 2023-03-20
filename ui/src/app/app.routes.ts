import {RouterModule} from "@angular/router";
import {ModuleWithProviders} from "@angular/core";
import {AppModule} from "./app.module";
import {AutoLoginPartialRoutesGuard} from "angular-auth-oidc-client";
import {CallbackComponent} from "./callback.component";

// Configuration of Router with all available AppConstants.Routes.
export const routing: ModuleWithProviders<AppModule> = RouterModule.forRoot([
    // Redirect for empty path.
    {path: "", redirectTo: "dashboard", pathMatch: "full"},
    // {path: "**", redirectTo: "dashboard"},
    {path: "callback", component: CallbackComponent},
    {
      path: "dashboard",
      loadChildren: () => import("./dashboard/dashboard.module").then(m => m.DashboardModule),
      canActivate: [AutoLoginPartialRoutesGuard]
    },
    {
      path: "audit",
      loadChildren: () => import("./audit/audit.module").then(m => m.AuditModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Audit"}
    },
    {
      path: "devices",
      loadChildren: () => import("./devices/devices.module").then(m => m.DevicesModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Devices"}
    },
    {
      path: "cas",
      loadChildren: () => import("./cas/cas.module").then(m => m.CasModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Certificate Authorities"}
    },
    {
      path: "certificates",
      loadChildren: () => import("./certificates/certificates.module").then(
        m => m.CertificatesModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Certificates"}
    },
    {
      path: "keystores",
      loadChildren: () => import("./keystores/keystores.module").then(m => m.KeystoresModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Keystores"}
    },
    {
      path: "infrastructure",
      loadChildren: () => import("./infrastructure/infrastructure.module").then(
        m => m.InfrastructureModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Infrastructure"}
    },
    {
      path: "provisioning",
      loadChildren: () => import("./provisioning/provisioning.module").then(
        m => m.ProvisioningModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Provisioning"}
    },
    {
      path: "settings",
      loadChildren: () => import("./settings/settings.module").then(m => m.SettingsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Settings"}
    },
    {
      path: "tags",
      loadChildren: () => import("./tags/tags.module").then(m => m.TagsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Tags"}
    },
    {
      path: "applications",
      loadChildren: () => import("./applications/applications.module").then(
        m => m.ApplicationsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Applications"}
    },
    {
      path: "command",
      loadChildren: () => import("./commands/commands.module").then(m => m.CommandsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Command"}
    },
    {
      path: "about",
      loadChildren: () => import("./about/about.module").then(m => m.AboutModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "About"}
    },
    {
      path: "campaigns",
      loadChildren: () => import("./campaigns/campaigns.module").then(m => m.CampaignsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Campaigns"}
    },
    {
      path: "dataflow",
      loadChildren: () => import("./dataflows/dataflows.module").then(m => m.DataflowsModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Dataflows"}
    },
    {
      path: "security",
      loadChildren: () => import("./security/security.module").then(m => m.SecurityModule),
      canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Security"}
    }
  ],
  {
    enableTracing: false,
    scrollPositionRestoration: "top"
  });
