import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {KeystoresListComponent} from "./keystores-list/keystores-list.component";
import {KeystoreEditComponent} from "./keystore-edit/keystore-edit.component";

const routes: Routes = [
  {path: "", component: KeystoresListComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: KeystoreEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Keystore " + route.params.id;
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class KeystoresRoutingModule {
}
