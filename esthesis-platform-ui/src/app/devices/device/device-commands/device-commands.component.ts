import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-device-commands',
  templateUrl: './device-commands.component.html',
  styleUrls: ['./device-commands.component.scss']
})
export class DeviceCommandsComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    console.log('commands...');
  }

}
