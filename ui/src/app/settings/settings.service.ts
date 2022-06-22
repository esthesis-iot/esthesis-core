import {Log} from 'ng2-logger/browser';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {SettingDto} from '../dto/setting-dto';
import {CrudService} from '../services/crud.service';
import {KeyValueDto} from '../dto/key-value-dto';
import {Observable} from 'rxjs';
import {FieldDto} from '../dto/field-dto';
import {environment} from 'src/environments/environment';

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: 'root'
})
export class SettingsService extends CrudService<SettingDto> {
  // Logger.
  private log = Log.create('SettingsService');

  constructor(http: HttpClient) {
    super(http, 'settings');
  }

  findByName(name: string): Observable<SettingDto> {
    return this.http.get<SettingDto>(
      `${environment.apiPrefix}/settings/byName?name=${name}`);
  }

  saveMultiple(keyValueDtos: KeyValueDto[]): Observable<any> {
    return this.http.post(
      `${environment.apiPrefix}/settings/byNames`, keyValueDtos);
  }

  findByNames(...names: string[]): Observable<SettingDto[]> {
    return this.http.get<SettingDto[]>(
      `${environment.apiPrefix}/settings/byNames?names=${names.join(',')}`);
  }

  getDevicePageFields(): Observable<FieldDto[]> {
    return this.http.get<FieldDto[]>(`${environment.apiPrefix}/settings/fields`);
  }

  saveDevicePageFields(form: any): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/settings/fields`, form);
  }
}
