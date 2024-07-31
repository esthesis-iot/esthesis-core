import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {SecurityRolesListComponent} from "./security-roles-list/security-roles-list.component";
import {SecurityRolesEditComponent} from "./security-roles-edit/security-roles-edit.component";
import {securityRoleNameResolver} from "../../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: SecurityRolesListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: SecurityRolesEditComponent, resolve: {
      breadcrumb: securityRoleNameResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityRolesRoutingModule { }
