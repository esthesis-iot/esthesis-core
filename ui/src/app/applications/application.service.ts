import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApplicationDto} from '../dto/application-dto';
import {CrudService} from '../services/crud.service';
import {environment} from "../../environments/environment";

/**
 * A service providing functionality for the user of the application, including authentication,
 * authorisation and session management.
 */
@Injectable({
  providedIn: 'root'
})
export class ApplicationService extends CrudService<ApplicationDto> {
  private static resource = `applications`;

  constructor(http: HttpClient) {
    super(http, ApplicationService.resource);
  }

  getApplicationsStatus(): Observable<any> {
    return this.http.get(
      `${environment.apiPrefix}/${ApplicationService.resource}/status`);
  }
}
