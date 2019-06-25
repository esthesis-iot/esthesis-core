import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {ApplicationDto} from '../dto/application-dto';
import {CrudService} from '../services/crud.service';
import {QFormsService} from '@eurodyn/forms';
import {CommandRequestDto} from '../dto/command-request-dto';

@Injectable({
  providedIn: 'root'
})
export class ControlService extends CrudService<CommandRequestDto> {
  // private resource = `control`;

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'control', qForms);
  }

  // getApplicationsStatus(): Observable<any> {
  //   return this.http.get(AppConstants.API_ROOT + `/${this.resource}` + '/status');
  // }
}
