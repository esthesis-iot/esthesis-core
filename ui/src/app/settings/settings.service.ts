import {Log} from "ng2-logger/browser";
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {FieldDto} from "../dto/field-dto";
import {environment} from "src/environments/environment";
import {RegistryEntryDto} from "../dto/registry-entry-dto";

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: "root"
})
export class SettingsService extends CrudService<RegistryEntryDto> {
  private log = Log.create("SettingsService");
  private static serviceContext = "v1/registry";

  constructor(http: HttpClient) {
    super(http, SettingsService.serviceContext);
  }

  findByNames(names: string): Observable<RegistryEntryDto[]> {
    return this.http.get<RegistryEntryDto[]>(
      `${environment.apiPrefix}/${SettingsService.serviceContext}/find/by-names/${names}`);
  }

  getDevicePageFields(): Observable<FieldDto[]> {
    return this.http.get<FieldDto[]>(`${environment.apiPrefix}/${SettingsService.serviceContext}/settings/fields`);
  }

  saveDevicePageFields(form: any): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/${SettingsService.serviceContext}/settings/fields`, form);
  }
}
