import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AboutComponent} from "./about.component";
import {AboutRoutingModule} from "./about-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {FlexModule} from "@angular/flex-layout";
import {MomentModule} from "ngx-moment";
import {MatCardModule} from "@angular/material/card";
import {MatTabsModule} from "@angular/material/tabs";
import {MatChipsModule} from "@angular/material/chips";

@NgModule({
  declarations: [AboutComponent],
  imports: [
    CommonModule,
    AboutRoutingModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    FlexModule,
    MatChipsModule,
    MomentModule
  ]
})
export class AboutModule {
}
