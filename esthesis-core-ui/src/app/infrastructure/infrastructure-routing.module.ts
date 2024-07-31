import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {InfrastructureComponent} from "./infrastructure.component";
import {
  InfrastructureMqttEditComponent
} from "./infrastructure-mqtt/infrastructure-mqtt-edit/infrastructure-mqtt-edit.component";
import {infraMqttNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: InfrastructureComponent, data: {breadcrumb: ""}},
  {path: "mqtt/:id", component: InfrastructureMqttEditComponent,
    resolve: {
      breadcrumb: infraMqttNameResolver
    }},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InfrastructureRoutingModule {
}
