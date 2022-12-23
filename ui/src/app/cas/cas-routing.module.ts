import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {CasListComponent} from "./cas-list/cas-list.component";
import {CasImportComponent} from "./cas-import/cas-import.component";
import {CasEditComponent} from "./cas-edit/cas-edit.component";

const routes: Routes = [
  {path: "", component: CasListComponent},
  {path: "import", component: CasImportComponent},
  {path: ":id", component: CasEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CasRoutingModule {
}
