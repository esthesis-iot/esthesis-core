import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ApplicationsListComponent} from "./applications-list/applications-list.component";
import {ApplicationEditComponent} from "./application-edit/application-edit.component";
import {applicationNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: ApplicationsListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: ApplicationEditComponent,
    resolve: {
      breadcrumb: applicationNameResolver
    }},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApplicationsRoutingModule {
}
