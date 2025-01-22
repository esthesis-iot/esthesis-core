import {Component} from "@angular/core";
import {DashboardUpdateSecurityStatsDto} from "../../dto/updates/DashboardUpdateSecurityStatsDto";
import {DashboardItemComponent} from "../dashboard-item.component";
import {
  DashboardItemSecurityStatsConfigurationDto
} from "../../dto/configuration/dashboard-item-security-stats-configuration-dto";

@Component({
  selector: 'app-dashboard-item-security-stats',
  templateUrl: './dashboard-item-security-stats.component.html'
})
export class DashboardItemSecurityStatsComponent
  extends DashboardItemComponent<DashboardUpdateSecurityStatsDto,
    DashboardItemSecurityStatsConfigurationDto> {
}
