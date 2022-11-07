import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SettingsService} from "../settings/settings.service";

@Injectable({
  providedIn: "root"
})
export class BodyBackgroundService {
  constructor(private httpClient: HttpClient, private settingsService: SettingsService) {
  }

  public getImageUrl(): Observable<ArrayBuffer> {
    return this.httpClient.get<string>(
      // @ts-ignore
      environment.apiPrefix + `/util/bg-photo`, {responseType: "text"});
  }
}
