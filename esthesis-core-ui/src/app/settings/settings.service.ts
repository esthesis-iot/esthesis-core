import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {Observable} from "rxjs";
import {DevicePageFieldDefinitionDto} from "./dto/device-page-field-definition-dto";
import {SettingDto} from "./dto/setting-dto";
import {AppConstants} from "../app.constants";

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: "root"
})
export class SettingsService extends CrudService<SettingDto> {
  private prefix = AppConstants.API_ROOT + "/settings/v1";

  constructor(http: HttpClient) {
    super(http, "settings/v1");
  }

  findByNames(names: string[]): Observable<SettingDto[]> {
    return this.http.get<SettingDto[]>(
      `${this.prefix}/find/by-names/${names.join(",")}`);
  }

  getDevicePageFields(): Observable<DevicePageFieldDefinitionDto[]> {
    return this.http.get<DevicePageFieldDefinitionDto[]>(`${this.prefix}/device-page-fields`);
  }

  saveDevicePageFields(form: any): Observable<any> {
    return this.http.post(`${this.prefix}/device-page-fields`, form);
  }

  findMeasurementNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.prefix}/find-measurement-names`);
  }
}
