import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommandComponent} from "./command.component";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {CommandReplyComponent} from "./command-reply/command-reply.component";

const routes: Routes = [
  {path: "", component: CommandComponent},
  {path: "create", component: CommandCreateComponent},
  {path: "reply/:id", component: CommandReplyComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandRoutingModule {
}
