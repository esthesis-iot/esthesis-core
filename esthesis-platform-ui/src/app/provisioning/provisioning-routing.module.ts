import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProvisioningComponent} from './provisioning.component';
import {ProvisioningEditComponent} from './provisioning-edit.component';
import {CanActivateGuard} from '../shared/guards/can-activate-guard';

const routes: Routes = [
  {path: '', component: ProvisioningComponent, canActivate: [CanActivateGuard]},
  {path: ':id', component: ProvisioningEditComponent, canActivate: [CanActivateGuard]},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule {
}
