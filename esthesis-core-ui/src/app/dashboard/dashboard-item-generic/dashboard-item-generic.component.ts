import {Component, Input} from "@angular/core";
import {DashboardWidgetDto} from "../dto/dashboard-widget-dto";

@Component({
  template: ``,
  standalone: true,
  selector: "app-dashboard-item-generic"
})
export class DashboardItemGenericComponent {
  @Input() widget!: DashboardWidgetDto;
  @Input() index!: number;
}
