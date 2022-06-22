import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SyncService {

  constructor(private httpClient: HttpClient) {
  }

  sync(): Observable<boolean> {
    return this.httpClient.post<boolean>(`${environment.apiPrefix}/sync`, "");
  }

  deleteWorkflow(): Observable<boolean> {
    return this.httpClient.delete<boolean>(`${environment.apiPrefix}/sync`);
  }

  clearQueues(): Observable<any> {
    return this.httpClient.post(`${environment.apiPrefix}/sync/clear-queues`, "");

  }
}
