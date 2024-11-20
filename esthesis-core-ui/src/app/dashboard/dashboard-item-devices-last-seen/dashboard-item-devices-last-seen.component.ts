import { Component } from '@angular/core';
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";

@Component({
  selector: 'app-dashboard-item-devices-last-seen',
  templateUrl: './dashboard-item-devices-last-seen.component.html'
})
export class DashboardItemDevicesLastSeenComponent extends DashboardItemGenericComponent {
  // < 1 min, <1 hr, <1 day, <1 month, older
  timeHistogram = [100, 22, 10, 4, 2]
  timeHistogramTotal = this.timeHistogram.reduce((a, b) => a + b, 0)


  protected readonly Math = Math;

  constructor() {
    super();
    console.log(this.timeHistogramTotal);
    console.log(this.timeHistogram[0]);
    console.log(this.timeHistogram[0] * 100 / this.timeHistogramTotal);
  }
}
