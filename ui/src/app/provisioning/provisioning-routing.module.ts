import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {ProvisioningEditComponent} from "./provisioning-edit/provisioning-edit.component";
import {ProvisioningListComponent} from "./provisioning-list/provisioning-list.component";

const routes: Routes = [
  {path: "", component: ProvisioningListComponent},
  {path: ":id", component: ProvisioningEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule {
}
