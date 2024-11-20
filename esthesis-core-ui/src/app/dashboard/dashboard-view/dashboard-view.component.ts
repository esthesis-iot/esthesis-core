import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {AppConstants} from "../../app.constants";
import {DashboardService} from "../dashboard.service";
import {MatDialog} from "@angular/material/dialog";
import {NgxMasonryComponent} from "ngx-masonry";
import {DashboardWidgetDto} from "../dto/dashboard-widget-dto";


@Component({
  selector: "app-dashboard-view",
  templateUrl: "./dashboard-view.component.html"
})
export class DashboardViewComponent extends BaseComponent implements OnInit, OnDestroy {
  @ViewChild(NgxMasonryComponent, {static: false}) masonry!: NgxMasonryComponent;
  dashboardItems: DashboardWidgetDto[] = [];
  constants = AppConstants;
  masonryOptions = {
    columnWidth: 100,
    // gutter: 10,
    horizontalOrder: true,
  }

  constructor(private dialog: MatDialog,
    private dashboardService: DashboardService) {
    super();
  }

  ngOnInit() {
    this.dashboardItems.push(
      {type: AppConstants.DASHBOARD.WIDGET.SENSOR, columns: 2, index: 0, title: "Main battery",
        subtitle:"Voltage", unit: "V", icon: "fa-bolt", precision: 2},
      {type: AppConstants.DASHBOARD.WIDGET.SENSOR_ICON, columns: 2, index: 0, title: "Sensor icon",
        subtitle:"Voltage", unit: "V", icon: "fa-bolt", precision: 2},
      {type: AppConstants.DASHBOARD.WIDGET.SENSOR, columns: 3, index: 1, title: "Main battery",
        unit: "A", icon: "fa-bolt", precision: 3},
      {type: AppConstants.DASHBOARD.WIDGET.SENSOR, columns: 2, index: 2, title: "Lab",
        subtitle:"Humidity", unit: "%", icon: "fa-water", precision: 4},
      {type: AppConstants.DASHBOARD.WIDGET.DEVICE_MAP, columns: 5, index: 12, title: "Device map"},
      {type: AppConstants.DASHBOARD.WIDGET.SECURITY_STATS, columns: 5, index: 3, title: "Security statistics",
        subtitle:"Project A"},
      {type: AppConstants.DASHBOARD.WIDGET.DEVICES_STATUS, columns: 4, index: 4, title: "Device status"},
      {type: AppConstants.DASHBOARD.WIDGET.DEVICES_LATEST, columns: 6, index: 5, title: "Latest devices"},
      {type: AppConstants.DASHBOARD.WIDGET.ABOUT, columns: 5, index: 6, title: "About"},
      {type: AppConstants.DASHBOARD.WIDGET.AUDIT, columns: 4, index: 7, title: "Audit"},
      {type: AppConstants.DASHBOARD.WIDGET.CAMPAIGNS, columns: 5, index: 8, title: "Campaigns"},
      {type: AppConstants.DASHBOARD.WIDGET.NOTES, columns: 3, index: 9, title: "Notes"},
      {type: AppConstants.DASHBOARD.WIDGET.TITLE, columns: 3, index: 10, title: "Title"},
      {type: AppConstants.DASHBOARD.WIDGET.DEVICES_LAST_SEEN, columns: 3, index: 11, title: "Last seen"},
    );
  }

  ngOnDestroy() {

  }

}
