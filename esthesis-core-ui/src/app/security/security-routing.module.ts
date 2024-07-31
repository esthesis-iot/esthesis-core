import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AutoLoginPartialRoutesGuard} from "angular-auth-oidc-client";

const routes: Routes = [
  {path: "users", loadChildren: () => import("./security-users/security-users.module").then(m => m.SecurityUsersModule),
    canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Users"}
  },
  {path: "groups", loadChildren: () => import("./security-groups/security-groups.module").then(m => m.SecurityGroupsModule),
    canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Groups"}
  },
  {path: "roles", loadChildren: () => import("./security-roles/security-roles.module").then(m => m.SecurityRolesModule),
    canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Roles"}
  },
  {path: "policies", loadChildren: () => import("./security-policies/security-policies.module").then(m => m.SecurityPoliciesModule),
    canActivate: [AutoLoginPartialRoutesGuard], data: {breadcrumb: "Policies"}
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityRoutingModule {
}
