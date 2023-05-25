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

const routes: Routes = [
  {path: "", component: DataflowsListComponent},
  {path: "new", component: DataflowNewComponent},
  {path: "wizards", component: DataflowWizardsListComponent},
  {path: "wizards/standard", component: DataflowWizardStandardComponent},
  {path: "edit/:type/:id", component: DataflowEditComponent}
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DataflowsRoutingModule {
}
