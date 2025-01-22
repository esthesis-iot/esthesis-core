import {Component, OnDestroy, OnInit} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {DashboardUpdateDeviceMapDto} from "../../dto/updates/DashboardUpdateDeviceMapDto";
import {latLng, Layer, LeafletMouseEvent, Map, MapOptions, marker, tileLayer} from "leaflet";
import {
  DashboardItemDeviceMapConfigurationDto
} from "../../dto/configuration/dashboard-item-device-map-configuration-dto";
import {Subscription} from "rxjs";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-dashboard-item-device-map",
  templateUrl: "./dashboard-item-device-map.component.html"
})
export class DashboardItemDeviceMapComponent
  extends DashboardItemComponent<DashboardUpdateDeviceMapDto, DashboardItemDeviceMapConfigurationDto>
  implements OnInit, OnDestroy {
  // A reference to the map component, obtained when the map is ready.
  private map!: Map;
  // Map needs to be initialized with a center, layer, and zoom, otherwise it will not be displayed.
  mapOptions: MapOptions = {
    layers: [
      tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
    ],
    zoom: 1,
    center: latLng(30, 0),
  };
  mapLayers: Layer[] = [];
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;
  // A flag to hide the map until the first message is received.
  mapVisible = false;
  // A flag to indicate the user has interacted with the map, so the view should not be changed anymore.
  mapInteracted = false;

  override ngOnInit() {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      this.mapVisible = true;

      // Add points to the map.
      if (lastMessage.coordinates.length > 0) {
        this.mapLayers = [];
        let lat!: number;
        let lon!: number;
        lastMessage.coordinates.forEach(coordinate => {
          lat = Number(coordinate.split(",")[1]);
          lon = Number(coordinate.split(",")[2]);
          this.mapLayers.push(
            marker([lat, lon], {icon: AppConstants.MAP_DEFAULT_ICON})
            .bindTooltip(coordinate.split(",")[0])
          );
        });

        if (!this.mapInteracted) {
          if (this.config?.mapLon && this.config?.mapLat) {
            this.map.setView(latLng(this.config.mapLat, this.config.mapLon), this.config.zoom);
          } else {
            this.map.setView(latLng(lat, lon), this.config!.zoom);
          }
        }
      }
    })
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

  onMapReady(map: Map): void {
    this.map = map;
  }

  onMouseDown($event: LeafletMouseEvent) {
    this.mapInteracted = true;
  }
}
