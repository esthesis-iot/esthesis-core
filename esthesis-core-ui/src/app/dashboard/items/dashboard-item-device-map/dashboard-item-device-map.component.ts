import {Component} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateDeviceMapDto} from "../../dto/updates/DashboardUpdateDeviceMapDto";

@Component({
  selector: "app-dashboard-item-device-map",
  templateUrl: "./dashboard-item-device-map.component.html"
})
export class DashboardItemDeviceMapComponent extends DashboardItemComponent<DashboardUpdateDeviceMapDto> {
}
