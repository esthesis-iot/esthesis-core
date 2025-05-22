import {Injectable} from "@angular/core";
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";
import {webSocket} from "rxjs/webSocket";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {Subject} from "rxjs";
import {ChatbotMessageDto} from "./dto/chatbot-message.dto";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class ChatbotService {
  // Web Socket connection to the server.
  private socket!: WebSocketSubject<any>;

  // Message callbacks for external components to handle events.
  private onMessageSubject = new Subject<ChatbotMessageDto>();
  onMessage$ = this.onMessageSubject.asObservable();
  private onErrorSubject = new Subject<any>();
  onError$ = this.onErrorSubject.asObservable();
  private onCompleteSubject = new Subject<void>();
  onComplete$ = this.onCompleteSubject.asObservable();

  constructor(private oidcSecurityService: OidcSecurityService) {
  }

  /**
   * Get the WebSocket URL based on the current environment.
   */
  private getUrl(): string {
    const protocol = window.location.protocol === "https:" ? "wss" : "ws";
    const host = window.location.hostname;
    const port = window.location.port ? `:${window.location.port}` : "";
    return `${protocol}://${host}${port}${AppConstants.API_ROOT}/chatbot/v1`;
  }

  connect(reconnect: boolean) {
    // If the socket is already connected, close it first.
    if (this.socket) {
      this.close();
    }

    // Initialize the WebSocket connection when the service is created after having
    // successfully authenticated the user and received an access token. The access token is passed
    // as a bearer token in the WebSocket connection by utilising the WebSocket protocol selection.
    this.oidcSecurityService.getAccessToken().subscribe({
      next: (token) => {
        this.socket = webSocket({
          url: this.getUrl(),
          protocol: ["bearer-token-carrier",
            encodeURIComponent("quarkus-http-upgrade#Authorization#Bearer " + token)],
          closeObserver: reconnect ? {
            next: (event: CloseEvent) => {
              this.onCompleteSubject.next();
              setTimeout(() => {
                this.connect(true);
              }, 1000);
            }
          } : undefined
        });

        this.socket.subscribe({
          next: (message: ChatbotMessageDto) => {
            this.onMessageSubject.next(message);
          },
          error: (err) => {
            this.onErrorSubject.next(err);
          },
          complete: () => {
            this.onCompleteSubject.next();
          }
        });
      },
      error: (err) => {
        console.error("Error getting access token.", err);
      }
    })
  }

  // Close the WebSocket connection
  private close() {
    this.socket.complete();
  }

  // Send a message back to the server via the WebSocket connection.
  sendMessage(message: ChatbotMessageDto) {
    this.socket.next(message);
  }
}
