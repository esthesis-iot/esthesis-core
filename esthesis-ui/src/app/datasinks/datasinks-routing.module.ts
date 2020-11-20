import {NgModule} from '@angular/core';
import {DatasinksComponent} from './datasinks.component';
import {DatasinksEditComponent} from './datasinks-edit.component';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {path: '', component: DatasinksComponent},
  {path: ':id', component: DatasinksEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DatasinksRoutingModule {
}
