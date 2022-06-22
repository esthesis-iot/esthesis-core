import {Injectable} from '@angular/core';
import {CrudService} from '../services/crud.service';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {DeviceRegisterDto} from '../dto/device-register-dto';
import {QPageableReply} from '@qlack/forms';
import {DeviceDto} from '../dto/device-dto';
import {FieldDto} from '../dto/field-dto';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class DevicesService extends CrudService<DeviceDto> {

  constructor(http: HttpClient) {
    super(http, 'devices');
  }

  preregister(ids: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/devices/preregister`, ids);
  }

  getDevices(queryString: string): Observable<QPageableReply<DeviceDto>> {
    return this.http.get<QPageableReply<DeviceDto>>(
      environment.apiPrefix + `/devices?${queryString}`);
  }

  downloadKeys(hardwareId: number) {
    this.http.get(`${environment.apiPrefix}/devices/${hardwareId}/keys`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  getDevicePageData(deviceId: number): Observable<any> {
    return this.http.get<FieldDto>(environment.apiPrefix + `/devices/device-page-data/${deviceId}`);
  }

  getDeviceDataField(deviceId: number, fields: string[]): Observable<FieldDto> {
    return this.http.get<FieldDto>(environment.apiPrefix +
      `/devices/device-data-field/${deviceId}?fields=${fields.join(",")}`);
  }

  findDeviceByPartialHardwareId(hardwareId: string): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>(
      environment.apiPrefix + `/devices/by-partial-hardware-id/${hardwareId}`);
  }
}
