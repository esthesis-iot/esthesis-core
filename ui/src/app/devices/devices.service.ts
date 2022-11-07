import {Injectable} from "@angular/core";
import {CrudService} from "../services/crud.service";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DeviceRegisterDto} from "../dto/device-register-dto";
import {DeviceDto} from "../dto/device-dto";
import {environment} from "../../environments/environment";
import {DeviceProfileNoteDto} from "../dto/device-profile-note-dto";

@Injectable({
  providedIn: "root"
})
export class DevicesService extends CrudService<DeviceDto> {

  constructor(http: HttpClient) {
    super(http, "v1/device");
  }

  preregister(devices: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/v1/device/preregister`, devices);
  }

  // getDevices(queryString: string): Observable<QPageableReply<DeviceDto>> {
  //   return this.http.get<QPageableReply<DeviceDto>>(
  //     environment.apiPrefix + `/device/${queryString}`);
  // }

  downloadKeys(hardwareId: string) {
    this.http.get(`${environment.apiPrefix}/device/${hardwareId}/keys`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  saveDeviceProfileNote(deviceId: string, profile: any) {
    return this.http.post(`${environment.apiPrefix}/v1/device/${deviceId}/device-profile`, profile);
  }

  getDeviceProfileNotes(deviceId: string): Observable<DeviceProfileNoteDto[]> {
    return this.http.get<DeviceProfileNoteDto[]>(
      environment.apiPrefix + `/v1/device/${deviceId}/device-profile`);
  }

  findDeviceByPartialHardwareId(hardwareId: string): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>(
      environment.apiPrefix + `/device/by-partial-hardware-id/${hardwareId}`);
  }

  addDeviceProfileNote(targetDeviceId: string, newFieldName: string, newFieldLabel: string) {
    const deviceProfileNoteDto: DeviceProfileNoteDto = {
      deviceId: targetDeviceId,
      fieldName: newFieldName,
      label: newFieldLabel
    };
    return this.http.post(
      `${environment.apiPrefix}/v1/device/device-profile/add-note`, deviceProfileNoteDto);
  }

  removeDeviceProfileNote(deviceId: string, keyName: string) {
    return this.http.delete(
      `${environment.apiPrefix}/v1/device/${deviceId}/device-profile/delete-note?keyName=${keyName}`);
  }
}
