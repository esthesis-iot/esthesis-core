import {Component, Inject} from "@angular/core";
import {latLng, Layer, MapOptions, marker, tileLayer} from "leaflet";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-framed-map",
  templateUrl: "./framed-map.component.html"
})
export class FramedMapComponent {
  mapOptions?: MapOptions;
  mapLayers?: Layer[];

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.mapOptions = {
      layers: [
        tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
      ],
      zoom: data.zoom,
      center: latLng(data.latitude, data.longitude)
    };
    this.mapLayers = [
      marker([data.latitude, data.longitude],
        { icon: AppConstants.MAP_DEFAULT_ICON})
    ];
  }

}
