import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {MatTabGroup} from "@angular/material/tabs";

@Component({
  selector: "app-infrastructure",
  templateUrl: "./infrastructure.component.html",
  styleUrls: []
})
export class InfrastructureComponent implements AfterViewInit {
  @ViewChild(MatTabGroup, {static: true}) tabs!: MatTabGroup;

  constructor(private activatedRoute: ActivatedRoute) {
  }

  ngAfterViewInit(): void {
    // Tab activation.
    const fragment = this.activatedRoute.snapshot.fragment;

    if (fragment === "mqtt") {
      this.tabs.selectedIndex = 0;
    } else if (fragment === "nifi") {
      this.tabs.selectedIndex = 1;
    }

  }

}
