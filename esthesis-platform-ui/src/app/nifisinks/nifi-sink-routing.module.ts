import {NgModule} from '@angular/core';
import {NiFiSinkComponent} from './nifi-sink.component';
import {NiFiSinkEditComponent} from './nifi-sink-edit.component';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {path: '', component: NiFiSinkComponent},
  {path: ':id', component: NiFiSinkEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NiFiSinkRoutingModule {
}
