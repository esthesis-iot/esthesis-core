import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {FieldDto} from '../../dto/field-dto';
import {AppConstants} from '../../app.constants';
import {HttpClient} from '@angular/common/http';
import {QFormsService} from '@eurodyn/forms';

@Injectable({
  providedIn: 'root'
})
export class DeviceMetadataService {

  constructor(private http: HttpClient, qForms: QFormsService) {
  }

  getMetadataFields(): Observable<FieldDto[]> {
    return this.http.get<FieldDto[]>(
      `${AppConstants.API_ROOT}/device-metadata/health/fields`);
  }

  save(form: any): Observable<any> {
    return this.http.post(`${AppConstants.API_ROOT}/device-metadata`, form);
  }

}
