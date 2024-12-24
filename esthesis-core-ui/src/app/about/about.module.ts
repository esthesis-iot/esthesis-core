import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AboutComponent} from "./about.component";
import {AboutRoutingModule} from "./about-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {MomentModule} from "ngx-moment";
import {MatCardModule} from "@angular/material/card";
import {MatTabsModule} from "@angular/material/tabs";
import {MatChipsModule} from "@angular/material/chips";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";

@NgModule({
  declarations: [AboutComponent],
  exports: [
    AboutComponent
  ],
  imports: [
    CommonModule,
    AboutRoutingModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    MatChipsModule,
    MomentModule,
    FontAwesomeModule
  ]
})
export class AboutModule {
}
