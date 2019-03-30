import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {CasComponent} from './cas.component';
import {CasImportComponent} from './cas-import.component';
import {CasEditComponent} from './cas-edit.component';

const routes: Routes = [
  {path: '', component: CasComponent},
  {path: 'import', component: CasImportComponent},
  {path: ':id', component: CasEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CasRoutingModule {
}
