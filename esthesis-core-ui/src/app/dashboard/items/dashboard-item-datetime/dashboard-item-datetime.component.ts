import {Component, OnDestroy, OnInit} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateDatetimeDto} from "../../dto/updates/DashboardUpdateDatetimeDto";
import {
  DashboardItemDatetimeConfigurationDto
} from "../../dto/configuration/dashboard-item-datetime-configuration-dto";
import moment from "moment";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-dashboard-item-datetime',
  templateUrl: './dashboard-item-datetime.component.html'
})
export class DashboardItemDatetimeComponent
  extends DashboardItemComponent<DashboardUpdateDatetimeDto, DashboardItemDatetimeConfigurationDto>
  implements OnInit, OnDestroy {

  public localDisplay?: string;
  public serverDisplay?: string;
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;

  override ngOnInit() {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      if (this.config?.date && !this.config?.time) {
        this.localDisplay = moment(new Date()).format(this.config.formatDate);
        this.serverDisplay = moment(this.lastMessage?.serverDate).format(this.config.formatDate);
      } else if (!this.config?.date && this.config?.time) {
        this.localDisplay = moment(new Date()).format(this.config.formatTime);
        this.serverDisplay = moment(this.lastMessage?.serverDate).format(this.config.formatTime);
      } else if (this.config?.date && this.config?.time) {
        this.localDisplay = moment(new Date()).format(this.config.formatDateTime);
        this.serverDisplay = moment(this.lastMessage?.serverDate).format(this.config.formatDateTime);
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
