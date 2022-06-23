import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FormGroup} from '@angular/forms';
import {CrudService} from '../services/crud.service';
import {CertificateDto} from '../dto/certificate-dto';
import {Observable} from 'rxjs';
import {QPageableReply} from '@qlack/forms';
import {environment} from "../../environments/environment";

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
      `${environment.apiPrefix}/certificates/${certificateId}/download/${keyType}/${base64}`, {
        responseType: 'blob', observe: 'response'
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  backup(caId: number) {
    this.http.get(`${environment.apiPrefix}/certificates/${caId}/backup`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  restore(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/certificates/restore`,
      false);
  }


  getAll(params?: string): Observable<QPageableReply<CertificateDto>> {
    if (!params) {
      return super.find('order=cn,asc');
    } else {
      return super.find(params);
    }
  }
}
