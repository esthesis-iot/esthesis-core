import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CampaignsComponent} from './campaigns.component';
import {CampaignEditComponent} from './campaign/campaign-edit/campaign-edit.component';

const routes: Routes = [
  {path: '', component: CampaignsComponent},
  {path: ':id', component: CampaignEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampaignsRoutingModule {
}
