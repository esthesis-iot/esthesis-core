import {Component, Input, OnInit} from "@angular/core";
import {AboutService} from "./about.service";
import {AboutDto} from "./dto/about-dto";
import {SecurityBaseComponent} from "../shared/components/security-base-component";
import {AppConstants} from "../app.constants";

@Component({
  selector: "app-about",
  templateUrl: "./about.component.html"
})
export class AboutComponent extends SecurityBaseComponent implements OnInit {
  @Input() embedded = false;
  about = {} as AboutDto;
  dtJsonUrl!: string;
  dtSwaggerUrl!: string;
  private readonly pageRouteName = "/about";

  constructor(private readonly aboutService: AboutService) {
    super(AppConstants.SECURITY.CATEGORY.ABOUT);
    // Find the location of this page, so that the Swagger URL can be constructed.
    let href = window.location.toString();
    href = href.substring(0, href.length - this.pageRouteName.length);

    this.dtJsonUrl = href + "/api/dt/openapi";
    this.dtSwaggerUrl = href + "/api/dt/openapi-ui/index.html";
  }

  ngOnInit() {
    this.aboutService.getAbout().subscribe(onNext => {
      this.about = onNext;
    });
  }
}
