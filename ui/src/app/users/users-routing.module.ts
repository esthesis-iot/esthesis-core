import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {UsersListComponent} from "./users-list/users-list.component";
import {UserEditComponent} from "./user-edit/user-edit.component";

const routes: Routes = [
  {path: "", component: UsersListComponent},
  {path: ":id", component: UserEditComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsersRoutingModule {
}
