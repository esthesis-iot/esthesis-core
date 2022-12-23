import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DataflowsListComponent} from "./dataflows-list/dataflows-list.component";
import {DataflowEditComponent} from "./dataflow-edit/dataflow-edit.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";

const routes: Routes = [
  {path: "", component: DataflowsListComponent},
  {path: "new", component: DataflowNewComponent},
  {path: "edit/:type/:id", component: DataflowEditComponent}
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DataflowsRoutingModule {
}
