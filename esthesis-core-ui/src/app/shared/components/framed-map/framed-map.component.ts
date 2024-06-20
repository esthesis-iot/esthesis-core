import {Component, Input, OnInit} from "@angular/core";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: "app-framed-map",
  templateUrl: "./framed-map.component.html"
})
export class FramedMapComponent implements OnInit {
  @Input() longitude!: number;
  @Input() latitude!: number;
  @Input() zoom = 13;
  @Input() width = "100%";
  @Input() height = "200";
  @Input() title?: string;
  geoUrl?: SafeResourceUrl;

  constructor(public sanitizer: DomSanitizer) {
  }

  ngOnInit(): void {
    this.geoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      `https://maps.google.com/maps?q=${this.latitude},${this.longitude}&z=${this.zoom}&output=embed`);
  }
}
