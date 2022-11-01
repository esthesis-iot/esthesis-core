import {Component, OnInit} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {BaseComponent} from "../../shared/component/base-component";

@Component({
  selector: "app-application-edit",
  templateUrl: "./application-edit.component.html",
  styleUrls: []
})
export class ApplicationEditComponent extends BaseComponent implements OnInit {
  id!: string | null;

  constructor(private route: ActivatedRoute) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id");
  }

}
