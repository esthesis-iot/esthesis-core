import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutComponent } from './about.component';
import {AboutRoutingModule} from './about-routing.module';
import {MatCardModule, MatIconModule, MatTabsModule} from '@angular/material';
import {FlexModule} from '@angular/flex-layout';

@NgModule({
  declarations: [AboutComponent],
  imports: [
    CommonModule,
    AboutRoutingModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    FlexModule
  ]
})
export class AboutModule { }
