import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ChatbotService} from "../chatbot.service";
import {v4 as uuidv4} from "uuid";
import {ChatbotMessageDto} from "../dto/chatbot-message.dto";
import _ from "lodash";
import {Subscription} from "rxjs";
import {SettingsService} from "../../settings/settings.service";
import {AppConstants} from "../../app.constants";
import {SettingDto} from "../../settings/dto/setting-dto";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: 'app-chatbot-view',
  templateUrl: './chatbot-view.component.html'
})
export class ChatbotViewComponent implements OnInit, OnDestroy {
  // A reference to the user input element to avoid maintaining a model.
  @ViewChild("userinput") userinput!: ElementRef;
  // A reference to the chatbox element to scroll to the bottom when a new message is added.
  @ViewChild("chatbox") chatbox!: ElementRef;
  // A flag to toggle the chatbox open and closed.
  state: boolean = false;
  // The messages to be displayed in the chatbox.
  messages = new Array<ChatbotMessageDto>();
  // Local subscriptions to WebSocket messages.
  onMessageSubscription?: Subscription;
  onErrorSubscription?: Subscription;
  onCompleteSubscription?: Subscription;
  waitingForAnswer: boolean = false;
  waitingForAnswerTimeout: any;
  enabled: boolean = false;

  constructor(private chatbotService: ChatbotService, private settingsService: SettingsService,
              private utilityService: UtilityService) {
  }

  ngOnInit(): void {
    // Subscribe to the chatbot service to receive messages and errors.
    this.onMessageSubscription = this.chatbotService.onMessage$.subscribe((data) => {
      this.onMessage(data);
    })
    this.onErrorSubscription = this.chatbotService.onError$.subscribe((err) => {
      this.onError(err);
    })
    this.onCompleteSubscription = this.chatbotService.onComplete$.subscribe(() => {
      this.onComplete();
    })

    this.settingsService.findByNames([AppConstants.NAMED_SETTING.CHATBOT_ENABLED])
    .subscribe({
      next: (settings: SettingDto[]) => {
        this.enabled = settings[0].value === "true";
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch settings.", onError);
      }
    });
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  /**
   * Placeholder function for formatting the message.
   * @param message The message to be formatted.
   * @private
   */
  formatMessage(message: string): string {
    return message;
  }

  /**
   * Scrolls the chatbox to the bottom.
   */
  scrollChatToBottom() {
    setTimeout(() => {
      this.chatbox.nativeElement.scrollTop = this.chatbox.nativeElement.scrollHeight;
    }, 100);
  }

  /**
   * Toggles the state of the chatbox. When the chatbox is open, it will focus on the user
   * input element and scroll to the bottom of the chatbox.
   */
  toggleState() {
    this.state = !this.state;

    setTimeout(() => {
      if (this.state && this.userinput?.nativeElement) {
        this.userinput.nativeElement.focus();
        this.scrollChatToBottom();
      }
    }, 100);
  }

  /**
   * Handles the incoming message from the chatbot service. It searches for a message with
   * the same correlationId and updates its message property if found. If not found, it
   * creates a new message.
   * @param message The message to be handled.
   */
  onMessage(message: ChatbotMessageDto) {
    clearTimeout(this.waitingForAnswerTimeout);
    // Replace <think> tags with empty strings.
    message.message = message.message.replace(/<think>[\s\S]*?<\/think>/g, "");

    // If the message is a system message, and it is identical to the previous one, ignore it.
    // This avoids flooding the chatbox with 'welcome' system messages on reconnect.
    if (!message.isUserInput && message.message === this.messages[this.messages.length - 1]?.message) {
      this.waitingForAnswer = false;
      return;
    }

    // Similarly, if the message is a repeated error message ignore it. This avoids flooding the
    // chatbox with reconnect messages.
    if (message.isError && message.message === this.messages[this.messages.length - 1]?.message) {
      this.waitingForAnswer = false;
      return;
    }

    // Append the message to an existing reply in the chatbox, or create a new reply.
    const index = _.findIndex(this.messages, (m) => m.correlationId === message.correlationId);
    if (index > -1) {
      this.messages[index].message = this.messages[index].message
        + this.formatMessage(message.message);
    } else {
      this.messages.push(message);
    }
    this.waitingForAnswer = false;
    this.scrollChatToBottom();
  }

  /**
   * Handles errors from the chatbot service. It logs the error to the chatbox.
   * @param err
   */
  onError(err
          :
          any
  ) {
    console.log("Error: ", err);
    clearTimeout(this.waitingForAnswerTimeout);
    this.waitingForAnswer = false;
  }

  /**
   * Handles the completion of the chatbot service. It logs a message to the chatbox and tries
   * to reconnect to the service.
   */
  onComplete() {
    clearTimeout(this.waitingForAnswerTimeout);
    const chatbotMessageDto: ChatbotMessageDto = {
      message: "Oops, it seems like the connection to the chatbot service was lost. I will try" +
        " to reconnect soon.",
      correlationId: uuidv4(),
      timestamp: Date.now(),
      isUserInput: false,
      isError: true
    }
    this.onMessage(chatbotMessageDto);
    this.waitingForAnswer = false;
  }

  /**
   * Sends a message to the chatbot service. It creates a new ChatbotMessageDto object with
   * the user input, correlationId, timestamp, and isUserInput properties. It then pushes
   * the message to the messages array, clears the user input field, scrolls the chatbox to the
   * bottom, and sends the message to the chatbot service via the WebSocket.
   */
  sendMessage() {
    const userInput = this.userinput.nativeElement.value;
    const chatbotMessageDto: ChatbotMessageDto = {
      message: userInput,
      correlationId: uuidv4(),
      timestamp: Date.now(),
      isUserInput: true
    }
    this.messages.push(chatbotMessageDto);
    this.scrollChatToBottom();
    this.chatbotService.sendMessage(chatbotMessageDto);
    this.userinput.nativeElement.value = '';

    clearTimeout(this.waitingForAnswerTimeout);
    setTimeout(() => {
      this.waitingForAnswer = true;
      this.scrollChatToBottom();
      this.waitingForAnswerTimeout = setTimeout(() => {
        this.waitingForAnswer = false;
      }, 30000);
    }, 200);
  }

  /**
   * Clears the chatbox by emptying the messages array and scrolling to the bottom. This method
   * does not clear the AI agent's memory.
   */
  clearChat() {
    this.messages = [];
    this.scrollChatToBottom();
  }

  /**
   * Disconnects from the chatbot service. It closes the WebSocket connection and
   * unsubscribes from the observables to avoid memory leaks.
   */
  disconnect() {
    // Unsubscribe from the chatbot service to avoid memory leaks.
    if (this.onMessageSubscription) {
      this.onMessageSubscription.unsubscribe();
    }
    if (this.onErrorSubscription) {
      this.onErrorSubscription.unsubscribe();
    }
    if (this.onCompleteSubscription) {
      this.onCompleteSubscription.unsubscribe();
    }
  }
}
