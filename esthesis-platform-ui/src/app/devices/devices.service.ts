import {Injectable} from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CaDto} from '../dto/ca-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';
import {Observable} from 'rxjs';
import {DeviceRegisterDto} from '../dto/device-register-dto';
import {QFormsService, QPageableReply} from '@eurodyn/forms';
import {DeviceDto} from '../dto/device-dto';
import {HttpUtilsService} from '../shared/service/http-utils.service';
import {FieldDto} from '../dto/field-dto';

@Injectable({
  providedIn: 'root'
})
export class DevicesService extends CrudService<CaDto> {

  constructor(http: HttpClient, private httpUtil: HttpUtilsService, qForms: QFormsService) {
    super(http, 'devices', qForms);
  }

  preregister(ids: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${AppConstants.API_ROOT}/devices/preregister`, ids);
  }

  getDevices(queryString: string): Observable<QPageableReply<DeviceDto>> {
    return this.http.get<QPageableReply<DeviceDto>>(
      AppConstants.API_ROOT + `/devices?${queryString}`);
  }

  downloadKeys(hardwareId: number) {
    this.http.get(`${AppConstants.API_ROOT}/devices/${hardwareId}/keys`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.httpUtil.saveAs(onNext);
    });
  }

  getFields(): Observable<FieldDto[]> {
    return this.http.get<FieldDto[]>(`${AppConstants.API_ROOT}/devices/fields`);
  }

  saveFields(form: any): Observable<any> {
    return this.http.post(`${AppConstants.API_ROOT}/devices/fields`, form);
  }

  getFieldValues(id: number): Observable<FieldDto> {
    return this.http.get<FieldDto>(AppConstants.API_ROOT + `/devices/field-values/${id}`);
  }
}
