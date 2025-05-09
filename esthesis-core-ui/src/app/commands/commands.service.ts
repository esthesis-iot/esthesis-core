import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../shared/services/crud.service";
import {CommandRequestDto} from "./dto/command-request-dto";
import {CommandReplyDto} from "./dto/command-reply-dto";
import {CommandExecuteRequestDto} from "./dto/command-execute-request-dto";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class CommandsService extends CrudService<CommandRequestDto> {
  private readonly prefix = AppConstants.API_ROOT + "/command/v1";

  constructor(http: HttpClient) {
    super(http, "command/v1");
  }

  getReply(requestId: string): Observable<CommandReplyDto[]> {
    return this.http.get<CommandReplyDto[]>(
      `${this.prefix}/reply/${requestId}`);
  }

  /**
   * Executes a command. Returns the correlation id of this command, so that results can be queried
   * later on.
   * @param data The command data for this command.
   */
  execute(data: CommandExecuteRequestDto): Observable<any> {
    return this.http.post<string>(`${this.prefix}`, data, {
      responseType: "text" as "json"
    });
  }

  deleteCommand(commandId: string): Observable<any> {
    return this.http.delete(`${this.prefix}/${commandId}`);
  }

  deleteReply(replyId: string): Observable<any> {
    return this.http.delete(`${this.prefix}/reply/${replyId}`);
  }

  deleteReplies(correlationId: string): Observable<any> {
    return this.http.delete(`${this.prefix}/reply/all/${correlationId}`);
  }

  /**
   * Purges command and replies older than the specified duration.
   * @param keepDuration The number of days to prior to which command and replies are deleted.
   */
  purge(keepDuration: number): Observable<any> {
    return this.http.delete(`${this.prefix}/purge/${keepDuration}`);
  }

  replayCommand(sourceCommandId: string) {
    return this.http.put(`${this.prefix}/replay/${sourceCommandId}`, null);
  }
}
