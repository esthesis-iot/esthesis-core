import {Component, OnDestroy, OnInit} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateDevicesLatestDto} from "../../dto/updates/DashboardUpdateDevicesLatestDto";
import {
  DashboardItemDevicesLatestConfigurationDto
} from "../../dto/configuration/dashboard-item-devices-latest-configuration-dto";
import {Subscription} from "rxjs";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-dashboard-item-devices-latest",
  templateUrl: "./dashboard-item-devices-latest.component.html"
})
export class DashboardItemDevicesLatestComponent
  extends DashboardItemComponent<DashboardUpdateDevicesLatestDto, DashboardItemDevicesLatestConfigurationDto>
  implements OnInit, OnDestroy {
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;


  override ngOnInit() {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      // An ugly hack to ensure that masonry layout is not broken when the number of devices
      // is less than the number of entries.
      if (lastMessage.devices.length < this.config!.entries) {
        for (let i = lastMessage.devices.length; i < this.config!.entries; i++) {
          lastMessage.devices.push({
            hardwareId: "",
            registeredOn: new Date(),
            type: AppConstants.DEVICE.TYPE.OTHER
          });
        }
      }
    });
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

}
