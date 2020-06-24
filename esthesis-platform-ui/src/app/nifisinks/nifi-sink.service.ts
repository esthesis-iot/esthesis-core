import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../services/crud.service';
import {QFormsService} from '@eurodyn/forms';
import {AppConstants} from '../app.constants';
import {NiFiSinkDto} from '../dto/nifisinks/nifi-sink-dto';

@Injectable({
  providedIn: 'root'
})
export class NifiSinkService extends CrudService<NiFiSinkDto> {
  private static endpoint = "nifi-sinks";

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, NifiSinkService.endpoint, qForms);
  }

  getAvailableDataFactoriesByType(type: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${AppConstants.API_ROOT}/${NifiSinkService.endpoint}/factories/${type}`);
  }
}
