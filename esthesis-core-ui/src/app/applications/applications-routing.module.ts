import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ApplicationsListComponent} from "./applications-list/applications-list.component";
import {ApplicationEditComponent} from "./application-edit/application-edit.component";

const routes: Routes = [
  {path: "", component: ApplicationsListComponent},
  {path: ":id", component: ApplicationEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApplicationsRoutingModule {
}
