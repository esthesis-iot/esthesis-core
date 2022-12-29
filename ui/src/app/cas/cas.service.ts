import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CaDto} from "./dto/ca-dto";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {UtilityService} from "../shared/service/utility.service";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";

@Injectable({
  providedIn: "root"
})
export class CasService extends CrudDownloadService<CaDto> {

  constructor(http: HttpClient, private utilityService: UtilityService, fs: FileSaverService) {
    super(http, "v1/ca", fs);
  }

  /**
   * Downloads the keys of a CA.
   * @param caId The id of the CA to download the keys of.
   * @param type The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: string, type: string) {
    this.http.get(
      `${environment.apiPrefix}/v1/ca/${caId}/download?type=${type}`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  import(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/v1/ca/import`, false);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${environment.apiPrefix}/v1/ca/eligible-for-signing`);
  }

}
