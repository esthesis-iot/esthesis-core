import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DeviceRegisterDto} from "./dto/device-register-dto";
import {DeviceDto} from "./dto/device-dto";
import {DevicePageFieldDataDto} from "./dto/device-page-field-data-dto";
import {GeolocationDto} from "./dto/geolocation-dto";
import {CrudDownloadService} from "../shared/services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {UtilityService} from "../shared/services/utility.service";
import {DeviceProfileDto} from "./dto/device-profile-dto";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class DevicesService extends CrudDownloadService<DeviceDto> {
  private prefix = AppConstants.API_ROOT + "/device/v1";

  constructor(http: HttpClient, fs: FileSaverService, private utilityService: UtilityService) {
    super(http, "device/v1", fs);
  }

  preregister(devices: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${this.prefix}/preregister`, devices);
  }

  download(deviceId: string, type: string) {
    this.http.get(`${this.prefix}/${deviceId}/download?type=${type}`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error downloading this key, please try again later.", error);
      }
    });
  }

  saveProfile(deviceId: string, profile: any) {
    return this.http.post(`${this.prefix}/${deviceId}/profile`, profile);
  }

  getProfile(deviceId: string): Observable<DeviceProfileDto> {
    return this.http.get<DeviceProfileDto>(`${this.prefix}/${deviceId}/profile`);
  }

  findDeviceByPartialHardwareId(hardwareId: string): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>(
      `${this.prefix}/find/by-hardware-id?hardwareIds=${hardwareId}&partialMatch=true`);
  }

  getDeviceData(deviceId: string): Observable<DevicePageFieldDataDto[]> {
    return this.http.get<DevicePageFieldDataDto[]>(
      `${this.prefix}/${deviceId}/device-data`);
  }

  getGeolocation(deviceId: string): Observable<GeolocationDto> {
    return this.http.get<GeolocationDto>(
      `${this.prefix}/${deviceId}/geolocation`);
  }
}
