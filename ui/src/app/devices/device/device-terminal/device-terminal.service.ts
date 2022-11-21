import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CommandExecuteRequestDto} from "../../../dto/command-execute-request-dto";
import {environment} from "src/environments/environment";
import {CommandReplyDto} from "../../../dto/command-reply-dto";

@Injectable({
  providedIn: "root"
})
export class DeviceTerminalService {

  constructor(private http: HttpClient) {
  }

  executeCommand(cmd: CommandExecuteRequestDto): Observable<CommandReplyDto[]> {
    return this.http.post<CommandReplyDto[]>(`${environment.apiPrefix}/v1/command/wait-for-reply`, cmd);
  }
}
