import {Component} from "@angular/core";
import {
  DashboardUpdateDevicesLastSeenDto
} from "../../dto/updates/DashboardUpdateDevicesLastSeenDto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: 'app-dashboard-item-devices-last-seen',
  templateUrl: './dashboard-item-devices-last-seen.component.html'
})
export class DashboardItemDevicesLastSeenComponent extends DashboardItemComponent<DashboardUpdateDevicesLastSeenDto> {
  // < 1 min, <1 hr, <1 day, <1 month, older
  timeHistogram = [100, 22, 10, 4, 2]
  timeHistogramTotal = this.timeHistogram.reduce((a, b) => a + b, 0)


  protected readonly Math = Math;
}
