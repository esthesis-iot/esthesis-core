import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppConstants} from '../../../app.constants';
import {CommandReplyDto} from '../../../dto/command-reply-dto';
import {CommandExecuteOrderDto} from '../../../dto/command-execute-order-dto';

@Injectable({
  providedIn: 'root'
})
export class DeviceTerminalService {

  constructor(private http: HttpClient) {
  }

  getReply(requestId: number): Observable<CommandReplyDto> {
    return this.http.get<CommandReplyDto>(
      `${AppConstants.API_ROOT}/command/reply-sync?requestId=${requestId}`);
  }

  executeCommand(data: CommandExecuteOrderDto): Observable<number> {
    return this.http.post<number>(`${AppConstants.API_ROOT}/command/execute-sync`, {
      command: 'EXECUTE',
      arguments: data.arguments,
      hardwareIds: data.hardwareIds
    });
  }

  getHardwareId(deviceId: number): Observable<string> {
    return this.http.get<string>(`${AppConstants.API_ROOT}/devices?${deviceId}`);
  }
}
