import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CaDto} from "./dto/ca-dto";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {UtilityService} from "../shared/services/utility.service";

@Injectable({
  providedIn: "root"
})
export class CasService extends CrudDownloadService<CaDto> {
  private prefix = environment.apiPrefix + "/ca/v1";

  constructor(http: HttpClient, private utilityService: UtilityService, fs: FileSaverService) {
    super(http, "ca/v1", fs);
  }

  /**
   * Downloads the keys of a CA.
   * @param caId The id of the CA to download the keys of.
   * @param type The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: string, type: string) {
    this.http.get(
      `${this.prefix}/${caId}/download?type=${type}`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  import(form: FormGroup) {
    return this.upload(form, `${this.prefix}/import`, false);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${this.prefix}/eligible-for-signing`);
  }

}
