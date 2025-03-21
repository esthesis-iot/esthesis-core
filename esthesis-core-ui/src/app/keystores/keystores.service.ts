import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudDownloadService} from "../shared/services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {KeystoreDto} from "./dto/keystore-dto";
import {Observable} from "rxjs";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class KeystoresService extends CrudDownloadService<KeystoreDto> {
  private readonly prefix = AppConstants.API_ROOT + "/crypto/keystore/v1";

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "crypto/keystore/v1", fs);
  }

  download(keystoreId: string) {
    this.http.get(
      `${this.prefix}/${keystoreId}/download`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  getSupportedKeystoreTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${AppConstants.API_ROOT}/crypto/crypto-info/v1/keystore-types`);
  }
}
