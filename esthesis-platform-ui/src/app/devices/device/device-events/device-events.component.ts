import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-device-events',
  templateUrl: './device-events.component.html',
  styleUrls: ['./device-events.component.scss']
})
export class DeviceEventsComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    console.log('events...');
  }

}
