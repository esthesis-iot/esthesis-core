import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {ApplicationDto} from '../dto/application-dto';
import {CrudService} from '../services/crud.service';
import {QFormsService} from '@eurodyn/forms';

/**
 * A service providing functionality for the user of the application, including authentication,
 * authorisation and session management.
 */
@Injectable({
  providedIn: 'root'
})
export class ApplicationService extends CrudService<ApplicationDto> {
  private resource = `applications`;

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'applications', qForms);
  }

  getApplicationsStatus(): Observable<any> {
    return this.http.get(AppConstants.API_ROOT + `/${this.resource}` + '/status');
  }
}
