import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AppConstants} from "../app.constants";
import {ChatMessageRequestDto} from "./dto/chat-message-request.dto";

@Injectable({
  providedIn: "root"
})
export class ChatbotService {
  private readonly prefix = AppConstants.API_ROOT + "/chatbot/v1";

  constructor(private readonly http: HttpClient) {
  }

  sendMessage(message: ChatMessageRequestDto): Observable<void> {
    return this.http.post<void>(`${this.prefix}`, message);
  }
}
