import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {HttpClient} from '@angular/common/http';
import {FormGroup} from '@angular/forms';
import {CrudService} from '../services/crud.service';
import {CertificateDto} from '../dto/certificate-dto';
import {Observable} from 'rxjs';
import {QPageableReply} from '@qlack/forms';

@Injectable({
  providedIn: 'root'
})
export class CertificatesService extends CrudService<CertificateDto> {

  constructor(http: HttpClient) {
    super(http, 'certificates');
  }

  /**
   * Downloads details about a certificate.
   * @param {number} certificateId The id of the certificate to download the details of.
   * @param {number} keyType The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(certificateId: number, keyType: number, base64: boolean) {
    this.http.get(
      `${AppConstants.API_ROOT}/certificates/${certificateId}/download/${keyType}/${base64}`, {
        responseType: 'blob', observe: 'response'
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  backup(caId: number) {
    // this.localHttp.get(`${AppConstants.API_ROOT}/cas/${caId}/backup`, {
    //   responseType: 'blob', observe: 'response'
    // }).subscribe(onNext => {
    //   this.httpUtil.saveAs(onNext);
    // });
  }

  restore(form: FormGroup) {
    // return this.qForms.uploadForm(this.localHttp, form, `${AppConstants.API_ROOT}/cas/restore`,
    // false);
  }


  getAll(params?: string): Observable<QPageableReply<CertificateDto>> {
    if (!params) {
      return super.getAll('order=cn,asc');
    } else {
      return super.getAll(params);
    }
  }
}
