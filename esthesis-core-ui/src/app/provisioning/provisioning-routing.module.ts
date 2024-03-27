import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {ProvisioningEditComponent} from "./provisioning-edit/provisioning-edit.component";
import {ProvisioningListComponent} from "./provisioning-list/provisioning-list.component";
import {AppConstants} from "../app.constants";

const routes: Routes = [
  {path: "", component: ProvisioningListComponent},
  {
    path: ":id", component: ProvisioningEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return route.params['id'] === AppConstants.NEW_RECORD_ID ?
          "New provisioning package" : "Provisioning package " + route.params['id'];
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule {
}
