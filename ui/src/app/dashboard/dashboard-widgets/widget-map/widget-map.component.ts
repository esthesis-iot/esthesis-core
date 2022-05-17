import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DashboardWidgetDto} from "../../../dto/dashboard-widget-dto";
import {Color} from "@angular-material-components/color-picker";
import {MatDialog} from "@angular/material/dialog";
import {DashboardService} from "../../dashboard.service";
import {FormatterService} from "../../../shared/service/formatter.service";
import {WidgetMapConf} from "./widget-map-conf";
import {WidgetMapSetupComponent} from "./widget-map-setup.component";
import {
  FeatureGroup,
  icon, LatLng,
  latLng,
  LatLngBoundsExpression,
  Layer,
  MapOptions,
  Marker,
  marker,
  tileLayer
} from "leaflet";
import {LeafletDirective} from "@asymmetrik/ngx-leaflet";
import {DeviceCoordinatesDto} from "../../../dto/device-coordinates-dto";

@Component({
  selector: 'app-widget-map',
  templateUrl: './widget-map.component.html',
  styleUrls: ['./widget-map.component.scss', '../common-widget-styling.scss']
})
export class WidgetMapComponent implements OnInit, OnDestroy, AfterViewInit {
  // the Id of the widget.
  @Input() id!: number
  // The Id of the dashboard this widget belongs to.
  @Input() dashboard!: number
  // The details of the widget.
  dashboardWidget!: DashboardWidgetDto;
  // A snapshot of widget's configuration. The snapshot is updated once when the widget is loaded.
  configuration!: WidgetMapConf;
  // Default colors.
  bgColor = new Color(64, 199, 247, 1);
  fgColor = new Color(255, 255, 255, 1);
  // The current/latest value for this widget.
  lastValue?: any;
  // Value updates subscription.
  lastValueSubscription?: any;
  @ViewChild(LeafletDirective, { static: true }) leaflet!: LeafletDirective;

  options: MapOptions = {
    layers: [
      tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18})
    ]
  };
  layer1 =  new Array<Marker>();
  fg = new FeatureGroup();

  constructor(private dialog: MatDialog, private dashboardService: DashboardService,
              private formatterService: FormatterService) {
  }

  getValue(centerMap: boolean) {
    // Get the current/latest value for this widget.
    this.dashboardService.getWidgetValue(this.id).subscribe(
      onNext => {
        const coords: DeviceCoordinatesDto[] = <DeviceCoordinatesDto[]><unknown>onNext;
        console.log(coords);
        this.layer1 = [];
        coords.forEach((coord) => {
          this.layer1.push(
            marker([coord.lat, coord.lon], {
              title: coord.hardwareId,
              riseOnHover: true
            }),
          )
        });
        this.fg.clearLayers();
        this.layer1.forEach(marker => {
          this.fg.addLayer(marker);
        })
        if (centerMap) {
          this.leaflet.getMap().fitBounds(this.fg.getBounds().pad(2));
        }
      }
    );
  }

  ngOnInit(): void {
    // Get the widget to display.
    this.dashboardService.getWidget(this.id).subscribe(onNext => {
      this.dashboardWidget = onNext;
      this.configuration = WidgetMapConf.deserialise(onNext.configuration);
      this.bgColor = this.formatterService.rgbaStringToColor(this.configuration.bgColor);
      this.fgColor = this.formatterService.rgbaStringToColor(this.configuration.fgColor);

      // Get current value.
      this.getValue(true);

      // Setup updates for widget's value.
      this.lastValueSubscription = setInterval(() => {
        this.getValue(false);
      // }, this.dashboardWidget.updateEvery * 1000);
      }, 10000);
    });
  }

  ngOnDestroy() {
    clearInterval(this.lastValueSubscription);
  }

  setup() {
    this.dialog.open(WidgetMapSetupComponent, {
      width: '35%',
      data: {
        id: this.id,
        dashboard: this.dashboard
      }
    });
  }

  ngAfterViewInit(): void {
    this.fg.addTo(this.leaflet.getMap());
    // Make sure the map correctly fits into its gridster item.
    window.dispatchEvent(new Event('resize'));
  }

}
