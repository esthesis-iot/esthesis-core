import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {DashboardItemDto} from "../dto/dashboard-item-dto";
import {Subject, Subscription} from "rxjs";
import {DashboardService} from "../dashboard.service";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  template: ``,
  standalone: true,
  selector: "app-dashboard-item"
})
export class DashboardItemComponent<T, K = unknown> implements OnInit, OnDestroy {
  // A reference to the concrete dashboard item extending this class.
  @Input() item!: DashboardItemDto;

  // The index of this item in the dashboard.
  @Input() index!: number;

  // The last message this component received.
  lastMessage?: T;

  // The configuration options of the component.
  config?: K;

  // The subscription to receive notifications on dashboard updates.
  emitterSub?: Subscription;

  // An emitter to notify child components of updates.
  lastMessageEmitter = new Subject<T>();

  constructor(private readonly dashboardService: DashboardService,
    readonly utilityService: UtilityService) {
  }

  ngOnInit(): void {
    if (this.item.configuration) {
      this.config = JSON.parse(this.item.configuration) as K;
    }

    this.emitterSub = this.dashboardService.getMessage().subscribe((message) => {
      if (message.id === this.item.id) {
        this.lastMessage = message as T;
        this.lastMessageEmitter.next(message as T);
      }
    });
  }

  ngOnDestroy() {
    if (this.emitterSub) {
      this.emitterSub.unsubscribe();
    }
  }

  /**
   * A helper function to get the value of the last message, applying the requested precision
   * specified in config if the value is a number.
   */
  getValue(): string | undefined {
    // If a number, round it to precision. If not, return it as is.
    // @ts-ignore
    if (this.lastMessage.value) {
      // @ts-ignore
      if (!isNaN(parseFloat(this.lastMessage.value))) {
        // @ts-ignore
        return parseFloat(this.lastMessage.value).toFixed(this.config.precision);
      } else {
        // @ts-ignore
        return this.lastMessage.value;
      }
    }

    return undefined
  }
}
