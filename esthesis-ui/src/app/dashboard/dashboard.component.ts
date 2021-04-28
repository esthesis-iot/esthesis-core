import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';
import {NiFiService} from '../infrastructure/infrastructure-nifi/nifi.service';
import {GridsterComponent, GridsterConfig} from "angular-gridster2";
import {MatDialog} from "@angular/material/dialog";
import {DashboardAddWidgetComponent} from "./dashboard-add-widget.component";
import {AppConstants} from "../app.constants";
import {WidgetSensorValueSetupComponent} from "./dashboard-widgets/widget-sensor-value/widget-sensor-value-setup.component";
import {DashboardService} from "./dashboard.service";
import {DashboardWidgetForGridDto} from "../dto/dashboard-widget-for-grid-dto";
import {Subscription} from "rxjs";
import {WidgetSensorGaugeComponent} from "./dashboard-widgets/widget-sensor-gauge/widget-sensor-gauge.component";
import {WidgetSensorGaugeSetupComponent} from "./dashboard-widgets/widget-sensor-gauge/widget-sensor-gauge-setup.component";


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit, OnDestroy {
  dashboardOptions!: GridsterConfig;
  dashboardWidgets!: Array<DashboardWidgetForGridDto>;
  private refreshSubscription: Subscription;
  // Expose application constants.
  constants = AppConstants;
  @ViewChild(GridsterComponent,
    {static: false}) gridsterComponent!: GridsterComponent;

  constructor(private nifiService: NiFiService, private dialog: MatDialog,
              private dashboardService: DashboardService) {
    super();

    // Subscribe to addition/removal of dashboard widgets.
    this.refreshSubscription = this.dashboardService.refreshDashboardObservable.subscribe(onNext => {
      this.getDashboardWidgets();
    });
  }

  ngOnInit() {
    //TODO this needs to be removed!
    this.nifiService.getActive().subscribe(value => {
      value
        ? localStorage.setItem("activeNiFi", value.id!.toString())
        : localStorage.removeItem("activeNiFi")
    });

    // Specify default layout options for the dashboard.
    this.dashboardOptions = {
      minCols: 8, maxCols: 8, minRows: 10,
      maxItemCols: 4, maxItemRows: 4,
      disableScrollVertical: true, disableScrollHorizontal: true,
      displayGrid: "none",
      compactType: "compactUp&Left",
      fixedRowHeight: 100,
      gridType: "verticalFixed",
      itemChangeCallback: this.itemChange.bind(this),
    };

    // Refresh widgets list.
    this.getDashboardWidgets();
  }

  itemChange(item: any, itemComponent: any) {
    const widgetId = itemComponent.el.id;
    if (widgetId) {
      this.dashboardService.updateWidgetCoordinates(widgetId, item.x, item.y, item.cols, item.rows).subscribe(
        onNext => {
        });
    }
  }

  ngOnDestroy() {
    this.refreshSubscription.unsubscribe();
  }

  getDashboardWidgets() {
    this.dashboardService.getWidgets().subscribe(
      onNext => {
        this.dashboardWidgets = onNext.map(widget => {
          return <DashboardWidgetForGridDto>{
            id: widget.id,
            type: widget.type,
            grid: {
              cols: widget.gridCols,
              rows: widget.gridRows,
              y: widget.gridY,
              x: widget.gridX,
              dragEnabled: true,
              resizeEnabled: true
            },
            dashboardId: widget.dashboard
          }
        });
      }
    );
  }

  addWidget() {
    this.dialog.open(DashboardAddWidgetComponent, {
      width: '40%',
    }).afterClosed().subscribe(result => {
      switch (result) {
        case AppConstants.DASHBOARD.WIDGETS.SENSOR_VALUE:
          this.dialog.open(WidgetSensorValueSetupComponent, {
            width: '40%',
            data: {
              id: 0
            }
          });
          break;
        case AppConstants.DASHBOARD.WIDGETS.SENSOR_GAUGE:
          this.dialog.open(WidgetSensorGaugeSetupComponent, {
            width: '40%',
            data: {
              id: 0
            }
          });
          break;
      }
    });
  }
}
