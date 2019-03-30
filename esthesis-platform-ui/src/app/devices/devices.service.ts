import {Injectable} from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CaDto} from '../dto/ca-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';
import {Observable} from 'rxjs';
import {DeviceRegisterDto} from '../dto/device-register-dto';
import {QPageableReply} from '@eurodyn/forms';
import {AuditDto} from '../dto/audit-dto';
import {DeviceDto} from '../dto/device-dto';

@Injectable({
  providedIn: 'root'
})
export class DevicesService extends CrudService<CaDto> {

  constructor(http: HttpClient) {
    super(http, 'devices');
  }

  preregister(ids: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${AppConstants.API_ROOT}/devices/preregister`, ids);
  }

  getDevices(queryString: string): Observable<QPageableReply<DeviceDto>> {
    return this.http.get<QPageableReply<DeviceDto>>(AppConstants.API_ROOT + `/devices?${queryString}`);
  }
}
