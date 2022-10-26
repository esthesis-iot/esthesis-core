import {RouterModule} from "@angular/router";
import {ModuleWithProviders} from "@angular/core";
import {NewPasswordComponent} from "./auth/new-password.component";
import {LogoutComponent} from "./auth/logout.component";
import {ForgotPasswordComponent} from "./auth/forgot-password.component";
import {AppModule} from "./app.module";
import {AutoLoginAllRoutesGuard} from "angular-auth-oidc-client";

// Configuration of Router with all available AppConstants.Routes.
export const routing: ModuleWithProviders<AppModule> = RouterModule.forRoot([
    // Redirect for empty path.
    {path: "", redirectTo: "dashboard", pathMatch: "full"},

    // Generic routes.
    {path: "new-password", component: NewPasswordComponent},
    {path: "logout", component: LogoutComponent},
    {path: "forgot-password", component: ForgotPasswordComponent},

    // Features routes.
    {
      path: "dashboard",
      loadChildren: () => import("./dashboard/dashboard.module").then(m => m.DashboardModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "audit",
      loadChildren: () => import("./audit/audit.module").then(m => m.AuditModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "devices",
      loadChildren: () => import("./devices/devices.module").then(m => m.DevicesModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "cas",
      loadChildren: () => import("./cas/cas.module").then(m => m.CasModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "certificates",
      loadChildren: () => import("./certificates/certificates.module").then(
        m => m.CertificatesModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "stores",
      loadChildren: () => import("./stores/stores.module").then(m => m.StoresModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "infra",
      loadChildren: () => import("./infrastructure/infrastructure.module").then(
        m => m.InfrastructureModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "provisioning",
      loadChildren: () => import("./provisioning/provisioning.module").then(
        m => m.ProvisioningModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "users",
      loadChildren: () => import("./users/users.module").then(m => m.UsersModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "settings",
      loadChildren: () => import("./settings/settings.module").then(m => m.SettingsModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "tags",
      loadChildren: () => import("./tags/tags.module").then(m => m.TagsModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "applications",
      loadChildren: () => import("./applications/applications.module").then(
        m => m.ApplicationsModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "command",
      loadChildren: () => import("./command/command.module").then(m => m.CommandModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "about",
      loadChildren: () => import("./about/about.module").then(m => m.AboutModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "data-wizards",
      loadChildren: () => import("./datawizards/datawizards.module").then(m => m.DatawizardsModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "campaigns",
      loadChildren: () => import("./campaigns/campaigns.module").then(m => m.CampaignsModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },
    {
      path: "dataflow",
      loadChildren: () => import("./dataflow/dataflow.module").then(m => m.DataflowModule),
      canActivate: [AutoLoginAllRoutesGuard]
    },

    // Redirect all other urls to Dashboard.
    {path: "**", redirectTo: "dashboard"}
  ],
  {
    enableTracing: false,
    scrollPositionRestoration: "top"
  });
