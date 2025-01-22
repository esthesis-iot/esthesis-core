import {Component} from "@angular/core";
import {DashboardUpdateDevicesStatusDto} from "../../dto/updates/DashboardUpdateDevicesStatusDto";
import {DashboardItemComponent} from "../dashboard-item.component";
import {
  DashboardItemDevicesStatusConfigurationDto
} from "../../dto/configuration/dashboard-item-devices-status-configuration-dto";

@Component({
  selector: "app-dashboard-item-devices-status",
  templateUrl: "./dashboard-item-devices-status.component.html"
})
export class DashboardItemDevicesStatusComponent
  extends DashboardItemComponent<DashboardUpdateDevicesStatusDto,
    DashboardItemDevicesStatusConfigurationDto> {
}
