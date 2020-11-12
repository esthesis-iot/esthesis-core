import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {StoresComponent} from './stores.component';
import {StoresEditComponent} from './stores-edit.component';

const routes: Routes = [
  {path: '', component: StoresComponent},
  {path: ':id', component: StoresEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StoresRoutingModule {
}
