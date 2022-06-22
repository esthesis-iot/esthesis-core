import {Log} from 'ng2-logger/browser';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {QFormsModule, QPageableReply} from '@qlack/forms';
import {AuditDto} from '../dto/audit-dto';
import {KeyValueDto} from '../dto/key-value-dto';
import {environment} from "../../environments/environment";

/**
 * A service providing functionality to view and manage the audit log.
 */
@Injectable({
  providedIn: 'root'
})
export class AuditService {
  // Logger.
  private log = Log.create('AuditService');

  constructor(private http: HttpClient, private qForms: QFormsModule) {
  }

  // Returns the audit logs.
  getLogs(queryString: string): Observable<QPageableReply<AuditDto>> {
    return this.http.get<QPageableReply<AuditDto>>(environment.apiPrefix + `/audit?${queryString}`);
  }

  getEvents(): Observable<KeyValueDto[]> {
    return this.http.get<KeyValueDto[]>(environment.apiPrefix + '/audit/events');
  }

  getLevels(): Observable<KeyValueDto[]> {
    return this.http.get<KeyValueDto[]>(environment.apiPrefix + '/audit/levels');
  }

}
