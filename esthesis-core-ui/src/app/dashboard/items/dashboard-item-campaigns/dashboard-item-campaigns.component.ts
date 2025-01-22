import {Component} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateCampaignDto} from "../../dto/updates/DashboardUpdateCampaignDto";

@Component({
  selector: 'app-dashboard-item-campaigns',
  templateUrl: './dashboard-item-campaigns.component.html'
})
export class DashboardItemCampaignsComponent extends DashboardItemComponent<DashboardUpdateCampaignDto> {

}
