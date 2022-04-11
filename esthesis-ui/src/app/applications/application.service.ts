import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {ApplicationDto} from '../dto/application-dto';
import {CrudService} from '../services/crud.service';

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
      `${AppConstants.API_ROOT}/${ApplicationService.resource}/status`);
  }
}
