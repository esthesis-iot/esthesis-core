import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DeviceRegisterDto} from "./dto/device-register-dto";
import {DeviceDto} from "./dto/device-dto";
import {environment} from "../../environments/environment";
import {DeviceProfileNoteDto} from "./dto/device-profile-note-dto";
import {DevicePageFieldDataDto} from "./dto/device-page-field-data-dto";
import {GeolocationDto} from "./dto/geolocation-dto";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {UtilityService} from "../shared/service/utility.service";

@Injectable({
  providedIn: "root"
})
export class DevicesService extends CrudDownloadService<DeviceDto> {
  private prefix = environment.apiPrefix + "/device/v1";

  constructor(http: HttpClient, fs: FileSaverService, private utilityService: UtilityService) {
    super(http, "device/v1", fs);
  }

  preregister(devices: DeviceRegisterDto): Observable<any> {
    return this.http.post(`${this.prefix}/preregister`, devices);
  }

  downloadPublicKey(hardwareId: string) {
    this.http.get(`${this.prefix}/${hardwareId}/download/public-key`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error downloading the public key, please try again later.", error);
      }
    });
  }

  downloadPrivateKey(hardwareId: string) {
    this.http.get(`${this.prefix}/${hardwareId}/download/private-key`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error downloading the private key, please try again later.", error);
      }
    });
  }

  downloadCertificate(hardwareId: string) {
    this.http.get(`${this.prefix}/${hardwareId}/download/certificate`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error downloading the certificate, please try again later.", error);
      }
    });
  }

  saveDeviceProfileNote(deviceId: string, profile: any) {
    return this.http.post(`${this.prefix}/${deviceId}/device-profile`, profile);
  }

  getDeviceProfileNotes(deviceId: string): Observable<DeviceProfileNoteDto[]> {
    return this.http.get<DeviceProfileNoteDto[]>(`${this.prefix}/${deviceId}/device-profile`);
  }

  findDeviceByPartialHardwareId(hardwareId: string): Observable<DeviceDto[]> {
    return this.http.get<DeviceDto[]>(
      `${this.prefix}/find/by-hardware-id?hardwareIds=${hardwareId}&partialMatch=true`);
  }

  addDeviceProfileNote(targetDeviceId: string, newFieldName: string, newFieldLabel: string) {
    const deviceProfileNoteDto: DeviceProfileNoteDto = {
      deviceId: targetDeviceId,
      fieldName: newFieldName,
      label: newFieldLabel
    };
    return this.http.post(
      `${this.prefix}/device-profile/add-note`, deviceProfileNoteDto);
  }

  removeDeviceProfileNote(deviceId: string, keyName: string) {
    return this.http.delete(
      `${this.prefix}/${deviceId}/device-profile/delete-note?keyName=${keyName}`);
  }

  getProfileFieldsData(deviceId: string): Observable<DevicePageFieldDataDto[]> {
    return this.http.get<DevicePageFieldDataDto[]>(
      `${this.prefix}/${deviceId}/device-profile/fields-data`);
  }

  getAllDeviceData(deviceId: string): Observable<DevicePageFieldDataDto[]> {
    return this.http.get<DevicePageFieldDataDto[]>(
      `${this.prefix}/${deviceId}/device-data`);
  }

  getGeolocation(deviceId: string): Observable<GeolocationDto> {
    return this.http.get<GeolocationDto>(
      `${this.prefix}/${deviceId}/geolocation`);
  }
}
