import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommandsListComponent} from "./commands-list/commands-list.component";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {CommandReplyComponent} from "./command-reply/command-reply.component";

const routes: Routes = [
  {path: "", component: CommandsListComponent, data: {breadcrumb: ""}},
  {path: "create", component: CommandCreateComponent, data: {breadcrumb: "New Command"}},
  {
    path: "reply/:id", component: CommandReplyComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Command " + route.params['id'];
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandsRoutingModule {
}
