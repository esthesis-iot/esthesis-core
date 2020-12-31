import {Injectable} from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CaDto} from '../dto/ca-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';
import {Observable} from 'rxjs';
import {DeviceRegisterDto} from '../dto/device-register-dto';
import {QPageableReply} from '@qlack/forms';
import {DeviceDto} from '../dto/device-dto';
import {FieldDto} from '../dto/field-dto';

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
    return this.http.get<QPageableReply<DeviceDto>>(
      AppConstants.API_ROOT + `/devices?${queryString}`);
  }

  downloadKeys(hardwareId: number) {
    this.http.get(`${AppConstants.API_ROOT}/devices/${hardwareId}/keys`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  getDevicePageData(deviceId: number): Observable<any> {
    return this.http.get<FieldDto>(AppConstants.API_ROOT + `/devices/device-page-data/${deviceId}`);
  }

  getDeviceDataField(deviceId: number, fields: string[]): Observable<FieldDto> {
    return this.http.get<FieldDto>(AppConstants.API_ROOT +
      `/devices/device-data-field/${deviceId}?fields=${fields.join(",")}`);
  }
}
