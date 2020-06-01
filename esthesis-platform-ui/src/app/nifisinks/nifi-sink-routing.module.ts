import {NgModule} from '@angular/core';
import {NifiSinkComponent} from './nifi-sink.component';
import {NiFiSinkEditComponent} from './ni-fi-sink-edit.component';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {path: '', component: NifiSinkComponent},
  {path: ':id', component: NiFiSinkEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NifiSinkRoutingModule {
}
