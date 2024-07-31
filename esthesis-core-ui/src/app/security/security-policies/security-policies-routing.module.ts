import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {
  SecurityPoliciesListComponent
} from "./security-policies-list/security-policies-list.component";
import {
  SecurityPoliciesEditComponent
} from "./security-policies-edit/security-policies-edit.component";
import {securityPolicyNameResolver} from "../../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: SecurityPoliciesListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: SecurityPoliciesEditComponent, resolve: {
      breadcrumb: securityPolicyNameResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityPoliciesRoutingModule { }
