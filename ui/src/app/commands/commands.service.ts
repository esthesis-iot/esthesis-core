import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../services/crud.service";
import {CommandRequestDto} from "./dto/command-request-dto";
import {CommandReplyDto} from "./dto/command-reply-dto";
import {environment} from "../../environments/environment";
import {CommandExecuteRequestDto} from "./dto/command-execute-request-dto";
import {DeviceDto} from "../devices/dto/device-dto";

@Injectable({
  providedIn: "root"
})
export class CommandsService extends CrudService<CommandRequestDto> {
  constructor(http: HttpClient) {
    super(http, "v1/command");
  }

  getReply(requestId: string): Observable<CommandReplyDto[]> {
    return this.http.get<CommandReplyDto[]>(
      `${environment.apiPrefix}/v1/command/reply/${requestId}`);
  }

  findDevicesByHardwareId(hardwareId: string): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>(`${environment.apiPrefix}/v1/command/find-devices/by-hardware-id`,
      {params: new HttpParams().set("hardwareId", hardwareId)});
  }

  /**
   * Executes a command. Returns the correlation id of this command, so that results can be queried
   * later on.
   * @param data The command data for this command.
   */
  execute(data: CommandExecuteRequestDto): Observable<any> {
    return this.http.post<string>(`${environment.apiPrefix}/v1/command`, data, {
      responseType: "text" as "json"
    });
  }

  deleteCommand(commandId: string): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/${commandId}`);
  }

  deleteReply(replyId: string): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/reply/${replyId}`);
  }

  deleteReplies(correlationId: string): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/reply/all/${correlationId}`);
  }

  /**
   * Purges command and replies older than the specified duration.
   * @param keepDuration The number of days to prior to which command and replies are deleted.
   */
  purge(keepDuration: number): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/purge/${keepDuration}`);
  }

  purgeAll(): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/purge`);
  }
}
