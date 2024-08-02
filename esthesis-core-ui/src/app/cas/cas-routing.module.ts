import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {CasListComponent} from "./cas-list/cas-list.component";
import {CasImportComponent} from "./cas-import/cas-import.component";
import {CasEditComponent} from "./cas-edit/cas-edit.component";
import {caNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: CasListComponent, data: {breadcrumb: ""}},
  {path: "import", component: CasImportComponent, data: {breadcrumb: "|Import certificate authority"}},
  {
    path: ":id", component: CasEditComponent,
    resolve: {
      breadcrumb: caNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CasRoutingModule {
}
