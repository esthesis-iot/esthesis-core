import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AppConstants} from '../../app.constants';

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  constructor(private httpClient: HttpClient) {
  }

  sync(): Observable<boolean> {
    return this.httpClient.post<boolean>(`${AppConstants.API_ROOT}/sync`, "");
  }

  deleteWorkflow(): Observable<boolean> {
    return this.httpClient.delete<boolean>(`${AppConstants.API_ROOT}/sync`);
  }

  clearQueues(): Observable<any>{
    return this.httpClient.post(`${AppConstants.API_ROOT}/sync/clear-queues`, "");

  }
}
