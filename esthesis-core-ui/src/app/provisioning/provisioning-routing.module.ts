import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {ProvisioningEditComponent} from "./provisioning-edit/provisioning-edit.component";
import {ProvisioningListComponent} from "./provisioning-list/provisioning-list.component";
import {provisioningPackageNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: ProvisioningListComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: ProvisioningEditComponent,
    resolve: {
      breadcrumb: provisioningPackageNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule {
}
