import {Log} from 'ng2-logger/browser';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {SettingDto} from '../dto/setting-dto';
import {CrudService} from '../services/crud.service';
import {KeyValueDto} from '../dto/key-value-dto';
import {Observable} from 'rxjs';
import {QFormsService} from '@eurodyn/forms';
import {FieldDto} from '../dto/field-dto';

/**
 * A service providing functionality to manage settings.
 */
@Injectable({
  providedIn: 'root'
})
export class SettingsService extends CrudService<SettingDto> {
  // Logger.
  private log = Log.create('SettingsService');

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'settings', qForms);
  }

  findByName(name: string): Observable<SettingDto> {
    return this.http.get<SettingDto>(
      `${AppConstants.API_ROOT}/settings/byName?name=${name}`);
  }

  saveMultiple(keyValueDtos: KeyValueDto[]): Observable<any> {
    return this.http.post(
      `${AppConstants.API_ROOT}/settings/byNames`, keyValueDtos);
  }

  findByNames(...names: string[]): Observable<SettingDto[]> {
    return this.http.get<SettingDto[]>(
      `${AppConstants.API_ROOT}/settings/byNames?names=${names.join(',')}`);
  }

}
