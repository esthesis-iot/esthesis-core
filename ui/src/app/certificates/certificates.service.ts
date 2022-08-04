import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {FormGroup} from "@angular/forms";
import {CrudService} from "../services/crud.service";
import {CertificateDto} from "../dto/certificate-dto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class CertificatesService extends CrudService<CertificateDto> {

  constructor(http: HttpClient) {
    super(http, "v1/certificate");
  }

  /**
   * Downloads details about a certificate.
   * @param {number} certificateId The id of the certificate to download the details of.
   */
  download(certificateId: string) {
    this.http.get(
      `${environment.apiPrefix}/v1/certificate/${certificateId}/download`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  import(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/v1/certificate/import`, false);
  }


  // getAll(params?: string): Observable<QPageableReply<CertificateDto>> {
  //   if (!params) {
  //     return super.find('order=cn,asc');
  //   } else {
  //     return super.find(params);
  //   }
  // }
}
