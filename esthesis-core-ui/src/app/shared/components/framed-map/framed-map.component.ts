import {Component, Inject, Input} from "@angular/core";
import {latLng, Layer, MapOptions, marker, tileLayer} from "leaflet";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";

@Component({
  selector: "app-framed-map",
  templateUrl: "./framed-map.component.html"
})
export class FramedMapComponent {
  @Input() longitude!: number;
  @Input() latitude!: number;
  @Input() zoom = 13;
  @Input() width = "100%";
  @Input() height = "200";
  @Input() title?: string;

  mapOptions?: MapOptions;
  mapLayers?: Layer[];

  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.mapOptions = {
      layers: [
        tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
      ],
      zoom: this.zoom,
      center: latLng(this.latitude, this.longitude)
    };
    this.mapLayers = [
      marker([this.latitude, this.longitude])
    ];
  }

}
