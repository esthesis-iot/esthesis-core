import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DataflowComponent} from "./dataflow.component";
import {DataflowEditComponent} from "./dataflow-edit/dataflow-edit.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";

const routes: Routes = [
  {path: "", component: DataflowComponent},
  {path: "new", component: DataflowNewComponent},
  {path: "edit/:type/:id", component: DataflowEditComponent}
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DataflowRoutingModule {
}
