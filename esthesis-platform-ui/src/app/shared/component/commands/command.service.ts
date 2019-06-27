import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AppConstants} from '../../../app.constants';

@Injectable({
  providedIn: 'root'
})
export class CommandService {

  constructor(private http: HttpClient) {
  }

  findDevicesByHardwareIds(hardwareIds: string): Observable<number> {
    return this.http.get<number>(`${AppConstants.API_ROOT}/devices/count/by-hardware-id`,
      {params: new HttpParams().set('hardwareIds', hardwareIds)});
  }

  findDevicesByTags(tags: string): Observable<number> {
    return this.http.get<number>(`${AppConstants.API_ROOT}/devices/count/by-tags`,
      {params: new HttpParams().set('tags', tags)});
  }

  findCommands(): Observable<string[]> {
    return this.http.get<string[]>(`${AppConstants.API_ROOT}/control/commands`);
  }

  execute(data: any): Observable<any> {
    return this.http.post<string[]>(`${AppConstants.API_ROOT}/control/execute`, data);
  }
}
