import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ProfileDetailsComponent} from './profile-details.component';

const routes: Routes = [
  {path: '', redirectTo: 'details', pathMatch: 'full'},
  {path: 'details', component: ProfileDetailsComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProfileRoutingModule {
}
