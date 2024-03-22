import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {UserProfileComponent} from "./user-profile/user-profile.component";
import {UserSignoutComponent} from "./user-signout/user-signout.component";

const routes: Routes = [
  {path: "profile", component: UserProfileComponent, data: {breadcrumb: "Profile"}},
  {path: "signout", component: UserSignoutComponent, data: {breadcrumb: "Sign Out"}}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
