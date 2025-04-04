import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ChatbotRoutingModule} from './chatbot-routing.module';
import {ChatbotViewComponent} from './chatbot-view/chatbot-view.component';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {MatTooltip} from "@angular/material/tooltip";


@NgModule({
  declarations: [
    ChatbotViewComponent
  ],
  exports: [
    ChatbotViewComponent
  ],
  imports: [
    CommonModule,
    ChatbotRoutingModule,
    FaIconComponent,
    MatTooltip
  ]
})
export class ChatbotModule { }
