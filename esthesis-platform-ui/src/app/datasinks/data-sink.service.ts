import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../services/crud.service';
import {QFormsService, QPageableReply} from '@eurodyn/forms';
import {DataSinkDto} from '../dto/data-sink-dto';
import {AppConstants} from '../app.constants';
import {DataSinkFactoryDto} from '../dto/data-sink-factory-dto';

@Injectable({
  providedIn: 'root'
})
export class DataSinkService extends CrudService<DataSinkDto> {
  private static endpoint = "data-sinks";

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, DataSinkService.endpoint, qForms);
  }

  getAll(params?: string): Observable<QPageableReply<DataSinkDto>> {
    if (!params) {
      return super.getAll('order=name,asc');
    } else {
      return super.getAll(params);
    }
  }

  getAvailableDataSinkFactories(): Observable<DataSinkFactoryDto[]> {
    return this.http.get<DataSinkFactoryDto[]>(`${AppConstants.API_ROOT}/${DataSinkService.endpoint}/factories`);
  }
}
