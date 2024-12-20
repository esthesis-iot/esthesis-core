import {Component} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateDevicesLatestDto} from "../../dto/updates/DashboardUpdateDevicesLatestDto";

@Component({
  selector: 'app-dashboard-item-devices-latest',
  templateUrl: './dashboard-item-devices-latest.component.html'
})
export class DashboardItemDevicesLatestComponent extends DashboardItemComponent<DashboardUpdateDevicesLatestDto> {

}
