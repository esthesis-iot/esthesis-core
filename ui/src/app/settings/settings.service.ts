import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {DevicePageFieldDto} from "../devices/dto/device-page-field-dto";
import {environment} from "src/environments/environment";
import {SettingDto} from "./dto/setting-dto";

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: "root"
})
export class SettingsService extends CrudService<SettingDto> {
  private prefix = environment.apiPrefix + "/settings/v1";

  constructor(http: HttpClient) {
    super(http, "settings/v1");
  }

  findByNames(names: string[]): Observable<SettingDto[]> {
    return this.http.get<SettingDto[]>(
      `${this.prefix}/find/by-names/${names.join(",")}`);
  }

  getDevicePageFields(): Observable<DevicePageFieldDto[]> {
    return this.http.get<DevicePageFieldDto[]>(`${this.prefix}/device-page-fields`);
  }

  saveDevicePageFields(form: any): Observable<any> {
    return this.http.post(`${this.prefix}/device-page-fields`, form);
  }

  findMeasurementNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.prefix}/find-measurement-names`);
  }
}
