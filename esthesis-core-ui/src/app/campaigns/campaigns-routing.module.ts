import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {CampaignsComponent} from "./campaigns-list/campaigns.component";
import {CampaignEditComponent} from "./campaign-edit/campaign-edit.component";
import {AppConstants} from "../app.constants";

const routes: Routes = [
  {path: "", component: CampaignsComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: CampaignEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return route.params['id'] === AppConstants.NEW_RECORD_ID ?
          "New campaign" : "Campaign " + route.params['id'];
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampaignsRoutingModule {
}
