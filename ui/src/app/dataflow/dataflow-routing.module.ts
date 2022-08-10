import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DataflowComponent} from "./dataflow.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";
import {DataflowMqttClientComponent} from "./dataflow-mqtt-client/dataflow-mqtt-client.component";

const routes: Routes = [
  {path: "", component: DataflowComponent},
  {path: "new", component: DataflowNewComponent},
  {path: "mqtt-client/:id", component: DataflowMqttClientComponent},
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DataflowRoutingModule {
}
