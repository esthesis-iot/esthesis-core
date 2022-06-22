import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {CommandExecuteOrderDto} from '../dto/command-execute-order-dto';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CommandCreateService {

  constructor(private http: HttpClient) {
  }

  findDevicesByHardwareIds(hardwareIds: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/devices/count/by-hardware-id`,
      {params: new HttpParams().set('hardwareIds', hardwareIds)});
  }

  findDevicesByTags(tags: string): Observable<number> {
    return this.http.get<number>(`${environment.apiPrefix}/devices/count/by-tags`,
      {params: new HttpParams().set('tags', tags)});
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
