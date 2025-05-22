import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ChatbotViewComponent} from './chatbot-view/chatbot-view.component';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {MatTooltip} from "@angular/material/tooltip";
import {MomentModule} from "ngx-moment";


@NgModule({
  declarations: [
    ChatbotViewComponent
  ],
  exports: [
    ChatbotViewComponent
  ],
  imports: [
    CommonModule,
    FaIconComponent,
    MatTooltip,
    MomentModule
  ]
})
export class ChatbotModule { }
