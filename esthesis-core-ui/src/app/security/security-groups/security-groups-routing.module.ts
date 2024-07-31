import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {SecurityGroupsListComponent} from "./security-groups-list/security-groups-list.component";
import {SecurityGroupsEditComponent} from "./security-groups-edit/security-groups-edit.component";
import {securityGroupNameResolver} from "../../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: SecurityGroupsListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: SecurityGroupsEditComponent,
    resolve: {
      breadcrumb: securityGroupNameResolver
    }},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityGroupsRoutingModule { }
