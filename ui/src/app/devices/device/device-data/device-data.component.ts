import { Component, OnInit } from "@angular/core";

@Component({
  selector: "app-device-data",
  templateUrl: "./device-data.component.html",
  styleUrls: ["./device-data.component.scss"]
})
export class DeviceDataComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
    console.log(">>>>>>>>>>>>>>>>>>>> DATA");
  }

}
