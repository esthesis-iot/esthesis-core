import {Component, OnDestroy, OnInit} from "@angular/core";
import {DashboardUpdateSensorIconDto} from "../../dto/updates/DashboardUpdateSensorIconDto";
import {DashboardItemComponent} from "../dashboard-item.component";
import {
  DashboardItemSensorIconConfigurationDto
} from "../../dto/configuration/dashboard-item-sensor-icon-configuration-dto";

@Component({
  selector: 'app-dashboard-item-sensor-icon',
  templateUrl: './dashboard-item-sensor-icon.component.html'
})
export class DashboardItemSensorIconComponent
  extends DashboardItemComponent<DashboardUpdateSensorIconDto, DashboardItemSensorIconConfigurationDto>
  implements OnInit, OnDestroy {

  override ngOnInit(): void {
    super.ngOnInit();
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
  }

  getIcon(): string | undefined {
    let icon = "question";

    if (this.config && this.lastMessage?.value) {
      for (const condition of this.config.conditions) {
        const conditionValue = condition.condition.replace("{val}", this.lastMessage?.value);
        if (eval(conditionValue)) {
          icon = condition.icon;
          break;
        }
      }
    }

    return icon;
  }

}
