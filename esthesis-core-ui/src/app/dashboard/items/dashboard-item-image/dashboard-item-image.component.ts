import {Component, OnDestroy, OnInit} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {
  DashboardItemImageConfigurationDto
} from "../../dto/configuration/dashboard-item-image-configuration-dto";
import {DashboardUpdateImageDto} from "../../dto/updates/DashboardUpdateImageDto";

@Component({
  selector: 'app-dashboard-item-image',
  templateUrl: './dashboard-item-image.component.html'
})
export class DashboardItemImageComponent
  extends DashboardItemComponent<DashboardUpdateImageDto, DashboardItemImageConfigurationDto>
  implements OnInit, OnDestroy {
  private refreshInterval: any;
  public updateUrl?: string;

  override ngOnInit(): void {
    super.ngOnInit();

    if (this.config?.imageUrl) {
      this.updateUrl = this.config.imageUrl;
    }

    if (this.config?.refresh && this.config?.refresh > 0) {
      this.refreshInterval = setInterval(() => {
        if (this.config?.imageUrl) {
          this.updateUrl = this.config?.imageUrl + "?t=" + new Date().getTime();
        }
      }, this.config.refresh * 1000);
    }
  }

  override ngOnDestroy(): void {
    clearInterval(this.refreshInterval);
    super.ngOnDestroy();
  }

}
