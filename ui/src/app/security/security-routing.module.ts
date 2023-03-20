import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {TagsListComponent} from "../tags/tags-list/tags-list.component";
import {
  SecurityUsersListComponent
} from "./security-users/security-users-list/security-users-list.component";
import {
  SecurityUsersEditComponent
} from "./security-users/security-users-edit/security-users-edit.component";
import {
  SecurityGroupsListComponent
} from "./security-groups/security-groups-list/security-groups-list.component";
import {
  SecurityGroupsEditComponent
} from "./security-groups/security-groups-edit/security-groups-edit.component";
import {
  SecurityRolesListComponent
} from "./security-roles/security-roles-list/security-roles-list.component";
import {
  SecurityRolesEditComponent
} from "./security-roles/security-roles-edit/security-roles-edit.component";
import {
  SecurityPoliciesListComponent
} from "./security-policies/security-policies-list/security-policies-list.component";
import {
  SecurityPoliciesEditComponent
} from "./security-policies/security-policies-edit/security-policies-edit.component";

const routes: Routes = [
  {path: "users", component: SecurityUsersListComponent, data: {breadcrumb: "Users"}},
  {path: "users/:id", component: SecurityUsersEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "User " + route.params.id;
      }
    }},
  {path: "groups", component: SecurityGroupsListComponent, data: {breadcrumb: "Groups"}},
  {path: "groups/:id", component: SecurityGroupsEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Group " + route.params.id;
      }
    }},
  {path: "roles", component: SecurityRolesListComponent, data: {breadcrumb: "Roles"}},
  {path: "roles/:id", component: SecurityRolesEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Role " + route.params.id;
      }
    }},
  {path: "policies", component: SecurityPoliciesListComponent, data: {breadcrumb: "Policies"}},
  {path: "policies/:id", component: SecurityPoliciesEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Policy " + route.params.id;
      }
    }}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityRoutingModule {
}
