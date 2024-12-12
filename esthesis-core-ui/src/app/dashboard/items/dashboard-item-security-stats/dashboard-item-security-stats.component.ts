import { Component } from '@angular/core';
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";

@Component({
  selector: 'app-dashboard-item-security-stats',
  templateUrl: './dashboard-item-security-stats.component.html'
})
export class DashboardItemSecurityStatsComponent extends DashboardItemGenericComponent {
  constructor() {
    super();
  }

}
