import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {CasListComponent} from "./cas-list/cas-list.component";
import {CasImportComponent} from "./cas-import/cas-import.component";
import {CasEditComponent} from "./cas-edit/cas-edit.component";

const routes: Routes = [
  {path: "", component: CasListComponent, data: {breadcrumb: ""}},
  {path: "import", component: CasImportComponent},
  {
    path: ":id", component: CasEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Certificate Authority " + route.params.id;
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CasRoutingModule {
}
