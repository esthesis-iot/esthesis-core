import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DataflowsListComponent} from "./dataflows-list/dataflows-list.component";
import {DataflowEditComponent} from "./dataflow-edit/dataflow-edit.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";
import {
  DataflowWizardsListComponent
} from "./dataflow-wizards/dataflow-wizards-list/dataflow-wizards-list.component";
import {
  DataflowWizardStandardComponent
} from "./dataflow-wizards/dataflow-wizard-standard/dataflow-wizard-standard.component";
import {dflNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: DataflowsListComponent, data: {breadcrumb: ""}},
  {path: "new", component: DataflowNewComponent, data: {breadcrumb: "New dataflow"}},
  {path: "wizards", component: DataflowWizardsListComponent},
  {path: "wizards/standard", component: DataflowWizardStandardComponent},
  {path: "edit/:type/:id", component: DataflowEditComponent,
    resolve: {
      breadcrumb: dflNameResolver
    }}
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DataflowsRoutingModule {
}
