import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CommandExecuteRequestDto} from "../../../commands/dto/command-execute-request-dto";
import {environment} from "src/environments/environment";
import {CommandReplyDto} from "../../../commands/dto/command-reply-dto";

@Injectable({
  providedIn: "root"
})
export class DeviceTerminalService {
  private prefix = environment.apiPrefix + "/command/v1";

  constructor(private http: HttpClient) {
  }

  executeCommand(cmd: CommandExecuteRequestDto): Observable<CommandReplyDto[]> {
    return this.http.post<CommandReplyDto[]>(`${this.prefix}/wait-for-reply`, cmd);
  }
}
