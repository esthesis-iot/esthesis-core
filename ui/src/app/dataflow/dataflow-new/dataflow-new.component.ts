import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";

@Component({
  selector: "app-dataflow-new",
  templateUrl: "./dataflow-new.component.html",
  styleUrls: ["./dataflow-new.component.scss"]
})
export class DataflowNewComponent extends BaseComponent implements OnInit {
  componentsList = [
    {
      title: "MQTT Client",
      category: "Data input",
      icon: "assets/img/dataflows/mqtt.png",
      description: "An MQTT client allows to fetch external messages into the platform by connecting to an MQTT broker.",
      action: "/dataflow/mqtt-client"
    }
  ];

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
