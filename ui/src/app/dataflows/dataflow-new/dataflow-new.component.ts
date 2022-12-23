import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";
import {dataflows} from "../dto/dataflow-definition";

@Component({
  selector: "app-dataflow-new",
  templateUrl: "./dataflow-new.component.html",
  styleUrls: ["./dataflow-new.component.scss"]
})
export class DataflowNewComponent extends BaseComponent implements OnInit {
  dataflows = dataflows;

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
