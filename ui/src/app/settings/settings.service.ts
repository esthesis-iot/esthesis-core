import {Log} from "ng2-logger/browser";
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {DevicePageFieldDto} from "../dto/device-page-field-dto";
import {environment} from "src/environments/environment";
import {SettingDto} from "../dto/setting-dto";

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: "root"
})
export class SettingsService extends CrudService<SettingDto> {
  private static serviceContext = "v1/settings";
  private log = Log.create("SettingsService");

  constructor(http: HttpClient) {
    super(http, SettingsService.serviceContext);
  }

  findByNames(names: string[]): Observable<SettingDto[]> {
    return this.http.get<SettingDto[]>(
      `${environment.apiPrefix}/${SettingsService.serviceContext}/find/by-names/${names.join(",")}`);
  }

  getDevicePageFields(): Observable<DevicePageFieldDto[]> {
    return this.http.get<DevicePageFieldDto[]>(`${environment.apiPrefix}/${SettingsService.serviceContext}/device-page-fields`);
  }

  saveDevicePageFields(form: any): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/${SettingsService.serviceContext}/device-page-fields`, form);
  }

  findMeasurementNames(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiPrefix}/${SettingsService.serviceContext}/find-measurement-names`);
  }
}
