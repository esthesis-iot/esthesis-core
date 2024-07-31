import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {KeystoresListComponent} from "./keystores-list/keystores-list.component";
import {KeystoreEditComponent} from "./keystore-edit/keystore-edit.component";
import {keystoreNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: KeystoresListComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: KeystoreEditComponent,
    resolve: {
      breadcrumb: keystoreNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class KeystoresRoutingModule {
}
