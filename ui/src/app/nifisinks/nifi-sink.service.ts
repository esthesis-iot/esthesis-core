import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../services/crud.service';
import {NiFiSinkDto} from '../dto/nifisinks/nifi-sink-dto';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class NifiSinkService extends CrudService<NiFiSinkDto> {
  private static endpoint = 'nifi-sinks';

  constructor(http: HttpClient) {
    super(http, NifiSinkService.endpoint);
  }

  getAvailableDataFactoriesByType(type: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${environment.apiPrefix}/${NifiSinkService.endpoint}/factories/${type}`);
  }
}
