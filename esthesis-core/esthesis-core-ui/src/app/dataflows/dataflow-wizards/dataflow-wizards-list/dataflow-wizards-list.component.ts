import {Component} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-dataflow-wizards-list",
  templateUrl: "./dataflow-wizards-list.component.html"
})
export class DataflowWizardsListComponent extends SecurityBaseComponent {

  constructor() {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW);
  }
}
