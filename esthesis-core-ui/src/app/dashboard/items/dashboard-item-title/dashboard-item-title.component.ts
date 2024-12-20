import {Component} from "@angular/core";
import {DashboardUpdateTitleDto} from "../../dto/updates/DashboardUpdateTitleDto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: 'app-dashboard-item-title',
  templateUrl: './dashboard-item-title.component.html'
})
export class DashboardItemTitleComponent extends DashboardItemComponent<DashboardUpdateTitleDto> {

}
