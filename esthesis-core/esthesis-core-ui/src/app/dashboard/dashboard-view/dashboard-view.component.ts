import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {GridsterComponent, GridsterConfig} from "angular-gridster2";
import {AppConstants} from "../../app.constants";
import {DashboardService} from "../dashboard.service";
import {DashboardWidgetForGridDto} from "../dto/dashboard-widget-for-grid-dto";
import {MatDialog} from "@angular/material/dialog";


@Component({
  selector: "app-dashboard-view",
  templateUrl: "./dashboard-view.component.html",
  styleUrls: ["./dashboard-view.component.scss"]
})
export class DashboardViewComponent extends BaseComponent implements OnInit, OnDestroy {
  dashboardOptions!: GridsterConfig;
  dashboardWidgets!: Array<DashboardWidgetForGridDto>;
  // private refreshSubscription: Subscription;
  // Expose application constants.
  constants = AppConstants;
  @ViewChild(GridsterComponent,
    {static: false}) gridsterComponent!: GridsterComponent;

  constructor(private dialog: MatDialog,
    private dashboardService: DashboardService) {
    super();

    // Subscribe to addition/removal of dashboard widgets.
    // this.refreshSubscription = this.dashboardService.refreshDashboardObservable.subscribe(onNext
    // => { this.getDashboardWidgets(); });
  }

  ngOnInit() {

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
    // this.getDashboardWidgets();
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
    // this.refreshSubscription.unsubscribe();
  }

  // getDashboardWidgets() {
  //   this.dashboardService.getWidgets().subscribe(
  //     onNext => {
  //       this.dashboardWidgets = onNext.map(widget => {
  //         return {
  //           id: widget.id,
  //           type: widget.type,
  //           grid: {
  //             cols: widget.gridCols,
  //             rows: widget.gridRows,
  //             y: widget.gridY,
  //             x: widget.gridX,
  //             dragEnabled: true,
  //             resizeEnabled: true
  //           },
  //           dashboardId: widget.dashboard
  //         } as DashboardWidgetForGridDto;
  //       });
  //     }
  //   );
  // }

}
