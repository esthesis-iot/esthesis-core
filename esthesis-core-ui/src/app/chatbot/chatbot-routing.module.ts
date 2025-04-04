import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ChatbotViewComponent} from "./chatbot-view/chatbot-view.component";

const routes: Routes = [
  {path: "", component: ChatbotViewComponent, data: {breadcrumb: ""}},
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChatbotRoutingModule { }
