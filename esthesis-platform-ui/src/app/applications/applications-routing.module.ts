import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {ApplicationsComponent} from './applications.component';
import {ApplicationEditComponent} from './application-edit/application-edit.component';

const routes: Routes = [
  {path: '', component: ApplicationsComponent},
  {path: ':id', component: ApplicationEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApplicationsRoutingModule {
}
