import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../services/crud.service';
import {QPageableReply} from '@eurodyn/forms';
import {DataSinkDto} from '../dto/data-sink-dto';

@Injectable({
  providedIn: 'root'
})
export class DataSinkService extends CrudService<DataSinkDto> {
  constructor(http: HttpClient) {
    super(http, 'data-sink');
  }

  getAll(params?: string): Observable<QPageableReply<DataSinkDto>> {
    if (!params) {
      return super.getAll('order=name,asc');
    } else {
      return super.getAll(params);
    }
  }
}
