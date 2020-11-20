import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DatawizardsComponent} from './datawizards.component';
import {DatawizardStandardComponent} from './datawizard-standard/datawizard-standard.component';

const routes: Routes = [
  {path: '', component: DatawizardsComponent},
  {path: 'standard', component: DatawizardStandardComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DatawizardsRoutingModule {
}
