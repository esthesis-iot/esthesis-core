import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommandComponent} from "./command.component";
import {CommandCreateComponent} from "./command-create.component";

const routes: Routes = [
  {path: "", component: CommandComponent},
  {path: "create", component: CommandCreateComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandRoutingModule {
}
