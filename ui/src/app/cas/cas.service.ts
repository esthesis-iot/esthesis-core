import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CaDto} from "../dto/ca-dto";
import {FormGroup} from "@angular/forms";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class CasService extends CrudService<CaDto> {

  constructor(http: HttpClient) {
    super(http, "v1/ca");
  }

  /**
   * Downloads details about the CA, such its keys and CERTIFICATE.
   * @param {number} caId The id of the CA to download the details of.
   * @param {number} keyType The type of the key to download as per AppConstants.KEY_TYPE.
   */
  download(caId: string, keyType: number, base64: boolean) {
    this.http.get(`${environment.apiPrefix}/cas/${caId}/download/${keyType}/${base64}`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  backup(caId: string) {
    this.http.get(`${environment.apiPrefix}/cas/${caId}/backup`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  restore(form: FormGroup) {
    return this.upload(form, `${environment.apiPrefix}/cas/restore`, false);
  }

  getEligibleForSigning(): Observable<CaDto[]> {
    return this.http.get<CaDto[]>(`${environment.apiPrefix}/v1/ca/eligible-for-signing`);
  }

}
