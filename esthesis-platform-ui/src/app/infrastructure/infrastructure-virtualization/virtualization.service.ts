import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {VirtualizationDto} from '../../dto/virtualization-dto';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {QPageableReply} from '@eurodyn/forms';

@Injectable({
  providedIn: 'root'
})
export class VirtualizationService extends CrudService<VirtualizationDto> {

  constructor(http: HttpClient) {
    super(http, 'virtualization');
  }

  getAll(params?: string): Observable<QPageableReply<VirtualizationDto>> {
    if (!params) {
      return super.getAll('order=name,asc');
    } else {
      return super.getAll(params);
    }
  }

}
