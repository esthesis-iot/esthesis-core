import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommandsListComponent} from "./commands-list/commands-list.component";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {CommandReplyComponent} from "./command-reply/command-reply.component";
import {commandsNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: CommandsListComponent, data: {breadcrumb: ""}},
  {path: "create", component: CommandCreateComponent, data: {breadcrumb: "New Command"}},
  {
    path: "reply/:id", component: CommandReplyComponent,
    resolve: {
      breadcrumb: commandsNameResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandsRoutingModule {
}
