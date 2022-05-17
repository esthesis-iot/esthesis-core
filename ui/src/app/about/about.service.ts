import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {AboutDto} from '../dto/about-dto';

@Injectable({
  providedIn: 'root'
})
export class AboutService {
  private resource = `about`;

  constructor(private http: HttpClient) {
  }

  getAbout(): Observable<AboutDto> {
    return this.http.get<AboutDto>(AppConstants.API_ROOT + `/${this.resource}`);
  }
}
