import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AboutDto} from "./dto/about-dto";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class AboutService {
  private readonly prefix = AppConstants.API_ROOT + "/about/v1";

  constructor(private readonly http: HttpClient) {
  }

  getAbout(): Observable<AboutDto> {
    return this.http.get<AboutDto>(`${this.prefix}/general`);
  }
}
