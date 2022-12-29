import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AboutComponent} from "./about.component";
import {AboutRoutingModule} from "./about-routing.module";
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatTabsModule} from "@angular/material/tabs";
import {FlexModule} from "@angular/flex-layout";
import {MatChipsModule} from "@angular/material/chips";
import {MomentModule} from "ngx-moment";

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
