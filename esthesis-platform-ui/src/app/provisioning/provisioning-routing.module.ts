import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ProvisioningComponent} from './provisioning.component';
import {CanActivateGuard} from '../guards/can-activate-guard';
import {ProvisioningEditComponent} from './provisioning-edit.component';

const routes: Routes = [
  {path: '', component: ProvisioningComponent, canActivate: [CanActivateGuard]},
  {path: ':id', component: ProvisioningEditComponent, canActivate: [CanActivateGuard]},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProvisioningRoutingModule { }
