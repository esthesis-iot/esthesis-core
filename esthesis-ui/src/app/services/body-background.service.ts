import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import 'rxjs-compat/add/operator/map';
import {Observable} from 'rxjs';
import {SettingsService} from '../settings/settings.service';
import {AppConstants} from '../app.constants';

@Injectable({
  providedIn: 'root'
})
export class BodyBackgroundService {
  constructor(private httpClient: HttpClient, private settingsService: SettingsService) {
  }

  public getImageUrl(): Observable<ArrayBuffer> {
    return this.httpClient.get<string>(
      // @ts-ignore
      AppConstants.API_ROOT + `/util/bg-photo`, {responseType: 'text'});
  }
}
