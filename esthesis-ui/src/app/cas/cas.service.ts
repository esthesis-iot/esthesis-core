import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {HttpClient} from '@angular/common/http';
import {CaDto} from '../dto/ca-dto';
import {FormGroup} from '@angular/forms';
import {CrudService} from '../services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CasService extends CrudService<CaDto> {

  constructor(http: HttpClient) {
    super(http, 'cas')
  }

  /**
   * Downloads details about the CA, such its keys and CERTIFICATE.
   * @param {number} caId The id of the CA to download the details of.
   * @param {number} keyType The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: number, keyType: number, base64: boolean) {
    this.http.get(`${AppConstants.API_ROOT}/cas/${caId}/download/${keyType}/${base64}`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  backup(caId: number) {
    this.http.get(`${AppConstants.API_ROOT}/cas/${caId}/backup`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  restore(form: FormGroup) {
    return this.upload(form, `${AppConstants.API_ROOT}/cas/restore`, false);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${AppConstants.API_ROOT}/cas/eligible-for-signing`);
  }

}
