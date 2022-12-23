import {Component, OnInit} from "@angular/core";
import {AboutService} from "./about.service";
import {AboutDto} from "./dto/about-dto";

@Component({
  selector: "app-about",
  templateUrl: "./about.component.html",
  styleUrls: ["./about.component.scss"]
})
export class AboutComponent implements OnInit {
  about!: AboutDto;
  dtJsonUrl!: string;
  dtSwaggerUrl!: string;
  private pageRouteName = "/about";

  constructor(private aboutService: AboutService) {
    // Find the location of this page, so that the Swagger URL can be constructed.
    let href = window.location.toString();
    href = href.substring(0, href.length - this.pageRouteName.length);
    this.dtJsonUrl = href + "/api/v3/api-docs";
    this.dtSwaggerUrl = href + "/api/swagger-ui.html";
  }

  ngOnInit() {
    this.aboutService.getAbout().subscribe(onNext => {
      this.about = onNext;
    });
  }

}
