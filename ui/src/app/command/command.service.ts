import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../services/crud.service";
import {CommandRequestDto} from "../dto/command-request-dto";
import {CommandReplyDto} from "../dto/command-reply-dto";
import {environment} from "../../environments/environment";
import {CommandExecuteOrderDto} from "../dto/command-execute-order-dto";

@Injectable({
  providedIn: "root"
})
export class CommandService extends CrudService<CommandRequestDto> {
  constructor(http: HttpClient) {
    super(http, "command");
  }

  getReply(requestId: number): Observable<CommandReplyDto> {
    return this.http.get<CommandReplyDto>(
      `${environment.apiPrefix}/v1/command/reply?requestId=${requestId}`);
  }

  findDevicesByHardwareIds(hardwareIds: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/v1/command/count-devices/by-hardware-id`,
      {params: new HttpParams().set("hardwareIds", hardwareIds)});
  }

  findDevicesByTags(tags: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/v1/command/count-devices/by-tags`,
      {params: new HttpParams().set("tags", tags)});
  }

  execute(data: CommandExecuteOrderDto): Observable<any> {
    return this.http.post<string[]>(`${environment.apiPrefix}/command/execute`, {
      command: data.command,
      arguments: data.arguments,
      description: data.description,
      tags: data.tags,
      hardwareIds: data.hardwareIds
    });
  }
}
