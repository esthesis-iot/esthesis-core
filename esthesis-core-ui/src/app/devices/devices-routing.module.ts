import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {DevicesComponent} from "./devices-list/devices.component";
import {DevicePreregisterComponent} from "./device-preregister/device-preregister.component";
import {DeviceComponent} from "./device/device.component";

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
    data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Device " + route.params.id;
      }
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DevicesRoutingModule {
}
