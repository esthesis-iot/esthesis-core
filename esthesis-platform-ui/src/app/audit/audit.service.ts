import {Log} from 'ng2-logger/browser';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {QFormsModule, QPageableReply} from '@eurodyn/forms';
import {AppConstants} from '../app.constants';
import {AuditDto} from '../dto/audit-dto';
import {KeyValueDto} from '../dto/key-value-dto';
import {UserDto} from '../dto/user-dto';

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
  getLogs(queryString: string):
    Observable<QPageableReply<AuditDto>> {
    return this.http.get<QPageableReply<AuditDto>>(AppConstants.API_ROOT + `/audit?${queryString}`);
  }

  getEvents(): Observable<string[]> {
    return this.http.get<string[]>(AppConstants.API_ROOT + '/audit/events');
  }

  getLevels(): Observable<string[]> {
    return this.http.get<string[]>(AppConstants.API_ROOT + '/audit/levels');
  }

  getUsers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(AppConstants.API_ROOT + '/audit/users');
  }
}
