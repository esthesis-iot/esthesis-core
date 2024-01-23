import {Component} from "@angular/core";
import {dataflows} from "../dto/dataflow-definitions/dataflow-definition";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-dataflow-new",
  templateUrl: "./dataflow-new.component.html"
})
export class DataflowNewComponent extends SecurityBaseComponent {
  dataflows = dataflows;

  constructor() {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW);
  }

}
