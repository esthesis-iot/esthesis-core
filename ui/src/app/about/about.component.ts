import {Component, OnInit} from "@angular/core";
import {AboutService} from "./about.service";
import {AboutDto} from "./dto/about-dto";

@Component({
  selector: "app-about",
  templateUrl: "./about.component.html",
  styleUrls: ["./about.component.scss"]
})
export class AboutComponent implements OnInit {
  about = {} as AboutDto;
  dtJsonUrl!: string;
  dtSwaggerUrl!: string;
  private pageRouteName = "/about";

  constructor(private aboutService: AboutService) {
    // Find the location of this page, so that the Swagger URL can be constructed.
    let href = window.location.toString();
    href = href.substring(0, href.length - this.pageRouteName.length);

    this.dtJsonUrl = href + "/api/dt/openapi";
    this.dtSwaggerUrl = href + "/api/dt/openapi-ui/";
  }

  ngOnInit() {
    this.aboutService.getAbout().subscribe(onNext => {
      this.about = onNext;
    });
  }

}
