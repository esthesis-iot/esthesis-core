import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {CampaignsComponent} from "./campaigns-list/campaigns.component";
import {CampaignEditComponent} from "./campaign-edit/campaign-edit.component";
import {campaignNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: CampaignsComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: CampaignEditComponent,
    resolve: {
      breadcrumb: campaignNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampaignsRoutingModule {
}
