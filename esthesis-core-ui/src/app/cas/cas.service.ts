import {Injectable} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {CaDto} from "./dto/ca-dto";
import {Observable} from "rxjs";
import {CrudDownloadService} from "../shared/services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {UtilityService} from "../shared/services/utility.service";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class CasService extends CrudDownloadService<CaDto> {
  private prefix = AppConstants.API_ROOT + "/crypto/ca/v1";

  constructor(http: HttpClient, private utilityService: UtilityService, fs: FileSaverService) {
    super(http, "crypto/ca/v1", fs);
  }

  /**
   * Downloads the keys of a CA.
   * @param caId The id of the CA to download the keys of.
   * @param type The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: string, type: string) {
    this.http.get(`${this.prefix}/${caId}/download?type=${type}`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error downloading this key, please try again later.", error);
      }
    });
  }

  import(json: any, files: Map<string, File | null>) {
    return this.upload(json, files, `${this.prefix}/import`);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${this.prefix}/eligible-for-signing`);
  }
}
