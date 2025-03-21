import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CertificateDto} from "./dto/certificate-dto";
import {CrudDownloadService} from "../shared/services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class CertificatesService extends CrudDownloadService<CertificateDto> {
  private readonly prefix = AppConstants.API_ROOT + "/crypto/certificate/v1";

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "crypto/certificate/v1", fs);
  }

  /**
   * Downloads they keys of a certificate.
   * @param certificateId The id of the certificate to download the keys of.
   * @param type The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(certificateId: string, type: string) {
    this.http.get(
      `${this.prefix}/${certificateId}/download?type=${type}`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  import(json: any, files: Map<string, File | null>) {
    return this.upload(json, files, `${this.prefix}/import`);
  }

}
