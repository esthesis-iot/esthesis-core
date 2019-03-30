import {Log} from 'ng2-logger/browser';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {SettingDto} from '../dto/setting-dto';
import {CrudService} from '../services/crud.service';

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

  findByName(name: string) {
    return this.http.get<SettingDto>(
      `${AppConstants.API_ROOT}/settings/byName?name=${name}`);
  }
}
