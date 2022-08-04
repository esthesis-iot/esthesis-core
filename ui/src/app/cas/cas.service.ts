import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CaDto} from "../dto/ca-dto";
import {FormGroup} from "@angular/forms";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {UtilityService} from "../shared/service/utility.service";

@Injectable({
  providedIn: "root"
})
export class CasService extends CrudService<CaDto> {

  constructor(http: HttpClient, private utilityService: UtilityService) {
    super(http, "v1/ca");
  }

  /**
   * Downloads details about the CA, such its keys and CERTIFICATE.
   * @param {number} caId The id of the CA to download the details of.
   * @param {number} keyType The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: string) {
    this.http.get(`${environment.apiPrefix}/v1/ca/${caId}/download`, {
      responseType: "blob", observe: "response"
    }).subscribe({
      next: (response) => {
        this.saveAs(response);
      }, error: (error) => {
        this.utilityService.popupError("There was an error downloading this CA, please try again later.");
      }
    });
  }

  import(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/v1/ca/import`, false);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${environment.apiPrefix}/v1/ca/eligible-for-signing`);
  }

}
