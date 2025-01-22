import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CommandExecuteRequestDto} from "../../../commands/dto/command-execute-request-dto";
import {CommandReplyDto} from "../../../commands/dto/command-reply-dto";
import {AppConstants} from "../../../app.constants";

@Injectable({
  providedIn: "root"
})
export class DeviceTerminalService {
  private readonly prefix = AppConstants.API_ROOT + "/command/v1";

  constructor(private readonly http: HttpClient) {
  }

  executeCommand(cmd: CommandExecuteRequestDto): Observable<CommandReplyDto[]> {
    return this.http.post<CommandReplyDto[]>(`${this.prefix}/wait-for-reply`, cmd);
  }

  executeCommandWithParams(cmd: CommandExecuteRequestDto, polling: number,
    timeout: number): Observable<CommandReplyDto[]> {
    return this.http.post<CommandReplyDto[]>(`${this.prefix}/wait-for-reply?timeout=${timeout}&pollInterval=${polling}`, cmd);
  }
}
