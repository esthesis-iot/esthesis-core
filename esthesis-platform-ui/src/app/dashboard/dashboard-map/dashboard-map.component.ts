import { Component, OnInit } from '@angular/core';
import {circle, icon, latLng, marker, polygon, tileLayer} from 'leaflet';

@Component({
  selector: 'app-dashboard-map',
  templateUrl: './dashboard-map.component.html',
  styleUrls: ['./dashboard-map.component.scss']
})
export class DashboardMapComponent implements OnInit {
  options = {
    layers: [
      tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18 })
    ],
    zoom: 8,
    center: latLng(47.2287109, 14.3009642)
  };

  layersControl = {
    overlays: {
      'Devices': circle([ 46.95, -122 ], { radius: 5000 }),
      'Energy': polygon([[ 46.8, -121.55 ], [ 46.9, -121.55 ], [ 46.9, -121.7 ], [ 46.8, -121.7 ]]),
      'Oil': polygon([[ 46.8, -121.55 ], [ 46.9, -121.55 ], [ 46.9, -121.7 ], [ 46.8, -121.7 ]]),
      'Water': polygon([[ 46.8, -121.55 ], [ 46.9, -121.55 ], [ 46.9, -121.7 ], [ 46.8, -121.7 ]]),
      'Gas': polygon([[ 46.8, -121.55 ], [ 46.9, -121.55 ], [ 46.9, -121.7 ], [ 46.8, -121.7 ]]),
    }
  };

  layer1 =[
    marker([47.2287109, 14.3009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
    marker([47.1287109, 14.1009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
    marker([47.0287109, 14.6009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
    marker([47.3287109, 14.4009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
    marker([47.7287109, 14.5009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
    marker([47.3287109, 14.6009642], {icon: icon({iconSize: [ 25, 41 ], iconAnchor: [ 13, 41 ], iconUrl: 'assets/marker-icon.png', shadowUrl: 'assets/marker-shadow.png'})}),
  ];

  constructor() { }

  ngOnInit() {
  }

}
