import {RouterModule} from "@angular/router";
import {ModuleWithProviders} from "@angular/core";
import {AppModule} from "./app.module";
import {AutoLoginAllRoutesGuard} from "angular-auth-oidc-client";

// Configuration of Router with all available AppConstants.Routes.
export const routing: ModuleWithProviders<AppModule> = RouterModule.forRoot([
    // Redirect for empty path.
    {path: "", redirectTo: "dashboard", pathMatch: "full"},
    {
      path: "dashboard",
      loadChildren: () => import("./dashboard/dashboard.module").then(m => m.DashboardModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "audit",
      loadChildren: () => import("./audit/audit.module").then(m => m.AuditModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Audit"}
    },
    {
      path: "devices",
      loadChildren: () => import("./devices/devices.module").then(m => m.DevicesModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Devices"}
    },
    {
      path: "cas",
      loadChildren: () => import("./cas/cas.module").then(m => m.CasModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Certificate Authorities"}
    },
    {
      path: "certificates",
      loadChildren: () => import("./certificates/certificates.module").then(
        m => m.CertificatesModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Certificates"}
    },
    {
      path: "stores",
      loadChildren: () => import("./stores/stores.module").then(m => m.StoresModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Stores"}
    },
    {
      path: "infrastructure",
      loadChildren: () => import("./infrastructure/infrastructure.module").then(
        m => m.InfrastructureModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Infrastructure"}
    },
    {
      path: "provisioning",
      loadChildren: () => import("./provisioning/provisioning.module").then(
        m => m.ProvisioningModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Provisioning"}
    },
    {
      path: "users",
      loadChildren: () => import("./users/users.module").then(m => m.UsersModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Users"}
    },
    {
      path: "settings",
      loadChildren: () => import("./settings/settings.module").then(m => m.SettingsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Settings"}
    },
    {
      path: "tags",
      loadChildren: () => import("./tags/tags.module").then(m => m.TagsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Tags"}
    },
    {
      path: "applications",
      loadChildren: () => import("./applications/applications.module").then(
        m => m.ApplicationsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Applications"}
    },
    {
      path: "command",
      loadChildren: () => import("./commands/commands.module").then(m => m.CommandsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Command"}
    },
    {
      path: "about",
      loadChildren: () => import("./about/about.module").then(m => m.AboutModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "About"}
    },
    {
      path: "campaigns",
      loadChildren: () => import("./campaigns/campaigns.module").then(m => m.CampaignsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Campaigns"}
    },
    {
      path: "dataflow",
      loadChildren: () => import("./dataflows/dataflows.module").then(m => m.DataflowsModule),
      canActivate: [AutoLoginAllRoutesGuard], data: {breadcrumb: "Dataflows"}
    },

    // Redirect all other urls to Dashboard.
    {path: "**", redirectTo: "dashboard"}
  ],
  {
    enableTracing: false,
    scrollPositionRestoration: "top"
  });
