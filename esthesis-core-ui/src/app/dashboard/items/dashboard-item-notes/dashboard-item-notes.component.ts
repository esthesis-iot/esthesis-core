import {Component} from "@angular/core";
import {DashboardUpdateNotesDto} from "../../dto/updates/DashboardUpdateNotesDto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: 'app-dashboard-item-notes',
  templateUrl: './dashboard-item-notes.component.html'
})
export class DashboardItemNotesComponent extends DashboardItemComponent<DashboardUpdateNotesDto> {

}
