import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {CommandComponent} from './command.component';

const routes: Routes = [
  {path: '', component: CommandComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandRoutingModule {
}
