import {Component, ElementRef, ViewChild} from '@angular/core';
import {ChatbotService} from "../chatbot.service";

@Component({
  selector: 'app-chatbot-view',
  templateUrl: './chatbot-view.component.html'
})
export class ChatbotViewComponent {
  @ViewChild("userinput") userinput!: ElementRef;
  @ViewChild("chatbox") chatbox!: ElementRef;
  state: boolean = true;

  constructor(private chatbotService: ChatbotService) {}

  toggleState() {
    this.state = !this.state;
    setTimeout(() => {
      if (this.state && this.userinput?.nativeElement) {
        this.userinput.nativeElement.focus();
        this.chatbox.nativeElement.scrollTop = this.chatbox.nativeElement.scrollHeight;
      }
    }, 100);
  }

  sendMessage() {
    var msg = this.userinput.nativeElement.value;
    // send msg
    this.userinput.nativeElement.value = '';
    this.chatbotService.sendMessage({
      message: msg,
      timestamp: new Date()
    }).subscribe({
      next: (next) => {

      }, error: (err) => {
        console.error("Error sending message", err);
      }
    });
  }
}
