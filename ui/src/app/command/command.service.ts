import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../services/crud.service";
import {CommandRequestDto} from "../dto/command-request-dto";
import {CommandReplyDto} from "../dto/command-reply-dto";
import {environment} from "../../environments/environment";
import {CommandExecuteRequestDto} from "../dto/command-execute-request-dto";

@Injectable({
  providedIn: "root"
})
export class CommandService extends CrudService<CommandRequestDto> {
  constructor(http: HttpClient) {
    super(http, "v1/command");
  }

  getReply(requestId: string): Observable<CommandReplyDto> {
    return this.http.get<CommandReplyDto>(
      `${environment.apiPrefix}/v1/command/reply/${requestId}`);
  }

  findDevicesByHardwareIds(hardwareIds: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/v1/command/count-devices/by-hardware-id`,
      {params: new HttpParams().set("hardwareIds", hardwareIds)});
  }

  findDevicesByTags(tags: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/v1/command/count-devices/by-tags`,
      {params: new HttpParams().set("tags", tags)});
  }

  execute(data: CommandExecuteRequestDto): Observable<any> {
    return this.http.post<string>(`${environment.apiPrefix}/v1/command`, {
      hardwareIds: data.hardwareIds,
      tags: data.tags,
      commandType: data.commandType,
      executionType: data.executionType,
      command: data.command,
      arguments: data.arguments,
      description: data.description,
    });
  }

  deleteCommand(commandId: string): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/${commandId}`);
  }

  deleteReply(replyId: string): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/v1/command/reply/${replyId}`);
  }
}
