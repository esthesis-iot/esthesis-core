import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {NiFiDto} from '../../dto/ni-fi-dto';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {WfVersionDto} from '../../dto/wf-version-dto';
import {AppConstants} from '../../app.constants';

@Injectable({
  providedIn: 'root'
})
export class NiFiService extends CrudService<NiFiDto> {

  constructor(http: HttpClient) {
    super(http, 'infrastructure/nifi');
  }

  getLatestWorkflowVersion(): Observable<WfVersionDto> {
    return this.http.get<WfVersionDto>(
      `${AppConstants.API_ROOT}/infrastructure/nifi/latest-wf-version`);
  }

  getActive(): Observable<NiFiDto> {
    return this.http.get<NiFiDto>(`${AppConstants.API_ROOT}/infrastructure/nifi/active`);
  }
}
