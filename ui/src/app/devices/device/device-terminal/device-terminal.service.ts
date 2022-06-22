import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CommandReplyDto} from '../../../dto/command-reply-dto';
import {CommandExecuteOrderDto} from '../../../dto/command-execute-order-dto';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DeviceTerminalService {

  constructor(private http: HttpClient) {
  }

  getReply(requestId: number): Observable<CommandReplyDto> {
    return this.http.get<CommandReplyDto>(
      `${environment.apiPrefix}/command/reply-sync?requestId=${requestId}&waitFor=5000`);
  }

  executeCommand(data: CommandExecuteOrderDto): Observable<number> {
    return this.http.post<number>(`${environment.apiPrefix}/command/execute-sync`, {
      command: 'EXECUTE',
      arguments: data.arguments,
      hardwareIds: data.hardwareIds
    });
  }

  getHardwareId(deviceId: number): Observable<string> {
    return this.http.get<string>(`${environment.apiPrefix}/devices?${deviceId}`);
  }
}
