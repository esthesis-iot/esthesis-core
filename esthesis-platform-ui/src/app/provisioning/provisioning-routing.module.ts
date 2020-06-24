import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProvisioningComponent} from './provisioning.component';
import {ProvisioningEditComponent} from './provisioning-edit.component';

const routes: Routes = [
  {path: '', component: ProvisioningComponent},
  {path: ':id', component: ProvisioningEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule {
}
