import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FooterComponent} from "./footer.component";
import {HeaderComponent} from "./header.component";
import {SidenavComponent} from "./sidenav.component";
import {MatIconModule} from "@angular/material/icon";
import {RouterLink, RouterModule} from "@angular/router";
import {MatMenuModule} from "@angular/material/menu";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    FooterComponent,
    HeaderComponent,
    SidenavComponent
  ],
  exports: [
    HeaderComponent,
    SidenavComponent,
    FooterComponent
  ],
  imports: [
    CommonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    RouterLink,
    MatButtonModule,
    RouterModule
  ]
})
export class LayoutModule {
}
