import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {InfrastructureComponent} from "./infrastructure.component";
import {
  InfrastructureMqttEditComponent
} from "./infrastructure-mqtt/infrastructure-mqtt-edit/infrastructure-mqtt-edit.component";

const routes: Routes = [
  {path: "", component: InfrastructureComponent},
  {path: "mqtt/:id", component: InfrastructureMqttEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InfrastructureRoutingModule {
}
