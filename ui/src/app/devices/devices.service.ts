import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DeviceRegisterDto} from "../dto/device-register-dto";
import {DeviceDto} from "../dto/device-dto";
import {environment} from "../../environments/environment";
import {DeviceProfileNoteDto} from "../dto/device-profile-note-dto";
import {DevicePageFieldDataDto} from "../dto/device-page-field-data-dto";
import {GeolocationDto} from "../dto/geolocation-dto";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";

@Injectable({
  providedIn: "root"
})
export class DevicesService extends CrudDownloadService<DeviceDto> {

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "v1/device", fs);
  }

  preregister(devices: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/v1/device/preregister`, devices);
  }

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

  getProfileFieldsData(deviceId: string): Observable<DevicePageFieldDataDto[]> {
    return this.http.get<DevicePageFieldDataDto[]>(
      environment.apiPrefix + `/v1/device/${deviceId}/device-profile/fields-data`);
  }

  getAllDeviceData(deviceId: string): Observable<DevicePageFieldDataDto[]> {
    return this.http.get<DevicePageFieldDataDto[]>(
      environment.apiPrefix + `/v1/device/${deviceId}/device-data`);
  }

  getGeolocation(deviceId: string): Observable<GeolocationDto> {
    return this.http.get<GeolocationDto>(
      environment.apiPrefix + `/v1/device/${deviceId}/geolocation`);
  }
}
