import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {StoresComponent} from './stores.component';
import {CertificateEditComponent} from '../certificates/certificate-edit.component';
import {StoresEditComponent} from './stores-edit.component';

const routes: Routes = [
  {path: '', component: StoresComponent},
  {path: ':id', component: StoresEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StoresRoutingModule { }
