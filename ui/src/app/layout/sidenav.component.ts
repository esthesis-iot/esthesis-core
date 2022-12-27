import {Component, ViewEncapsulation} from "@angular/core";
import {BaseComponent} from "../shared/component/base-component";

@Component({
  selector: "app-sidenav",
  templateUrl: "./sidenav.component.html",
  styleUrls: ["./sidenav.component.scss"],
  encapsulation: ViewEncapsulation.None
})
export class SidenavComponent extends BaseComponent {

  constructor() {
    super();
  }

}
