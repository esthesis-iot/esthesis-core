import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AboutDto} from "../dto/about-dto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class AboutService {
  private resource = `about`;

  constructor(private http: HttpClient) {
  }

  getAbout(): Observable<AboutDto> {
    return this.http.get<AboutDto>(environment.apiPrefix + `/${this.resource}`);
  }
}
