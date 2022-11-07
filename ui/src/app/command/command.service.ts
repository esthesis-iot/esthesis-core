import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../services/crud.service";
import {CommandRequestDto} from "../dto/command-request-dto";
import {CommandReplyDto} from "../dto/command-reply-dto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class CommandService extends CrudService<CommandRequestDto> {
  private resource = `command`;

  constructor(http: HttpClient) {
    super(http, "command");
  }

  getReply(requestId: number): Observable<CommandReplyDto> {
    return this.http.get<CommandReplyDto>(
      `${environment.apiPrefix}/${this.resource}/reply?requestId=${requestId}`);
  }
}
