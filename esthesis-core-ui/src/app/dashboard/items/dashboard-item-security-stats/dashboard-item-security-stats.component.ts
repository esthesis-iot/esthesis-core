import {Component} from "@angular/core";
import {DashboardUpdateSecurityStatsDto} from "../../dto/updates/DashboardUpdateSecurityStatsDto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: 'app-dashboard-item-security-stats',
  templateUrl: './dashboard-item-security-stats.component.html'
})
export class DashboardItemSecurityStatsComponent extends DashboardItemComponent<DashboardUpdateSecurityStatsDto> {


}
