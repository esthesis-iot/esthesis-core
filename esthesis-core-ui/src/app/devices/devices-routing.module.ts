import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DevicesComponent} from "./devices-list/devices.component";
import {DevicePreregisterComponent} from "./device-preregister/device-preregister.component";
import {DeviceComponent} from "./device/device.component";
import {deviceNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {
    path: "", component: DevicesComponent,
    data: {breadcrumb: ""}
  },
  {
    path: "preregister", component: DevicePreregisterComponent,
    data: {breadcrumb: "Preregister device"}
  },
  {
    path: ":id", component: DeviceComponent,
    resolve: {
      breadcrumb: deviceNameResolver
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DevicesRoutingModule {
}
