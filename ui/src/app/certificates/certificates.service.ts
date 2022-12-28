import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {FormGroup} from "@angular/forms";
import {CertificateDto} from "./dto/certificate-dto";
import {environment} from "../../environments/environment";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";

@Injectable({
  providedIn: "root"
})
export class CertificatesService extends CrudDownloadService<CertificateDto> {

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "v1/certificate", fs);
  }

  /**
   * Downloads details about a certificate.
   * @param {number} certificateId The id of the certificate to download the details of.
   * @param {string} type The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(certificateId: string, type: string) {
    this.http.get(
      `${environment.apiPrefix}/v1/certificate/${certificateId}/download?type=${type}`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  import(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/v1/certificate/import`, false);
  }

}
