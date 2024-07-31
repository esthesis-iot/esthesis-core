import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {SecurityUsersListComponent} from "./security-users-list/security-users-list.component";
import {SecurityUsersEditComponent} from "./security-users-edit/security-users-edit.component";
import {userNameResolver} from "../../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: SecurityUsersListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: SecurityUsersEditComponent,
    resolve: {
      breadcrumb: userNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SecurityUsersRoutingModule { }
