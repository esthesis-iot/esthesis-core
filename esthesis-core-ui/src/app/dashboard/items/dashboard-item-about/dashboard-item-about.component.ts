import {Component} from "@angular/core";
import {DashboardUpdateAboutDto} from "../../dto/updates/DashboardUpdateAboutDto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: 'app-dashboard-item-about',
  templateUrl: './dashboard-item-about.component.html'
})
export class DashboardItemAboutComponent extends DashboardItemComponent<DashboardUpdateAboutDto> {

}
