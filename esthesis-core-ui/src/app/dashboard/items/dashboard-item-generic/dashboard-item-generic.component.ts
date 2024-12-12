import {Component, Input} from "@angular/core";
import {DashboardItemDto} from "../../dto/view-edit/dashboard-item-dto";

@Component({
  template: ``,
  standalone: true,
  selector: "app-dashboard-item-generic"
})
export class DashboardItemGenericComponent {
  @Input() item!: DashboardItemDto;
  @Input() index!: number;
}
