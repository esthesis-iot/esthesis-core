import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ProvisioningDto} from "../dto/provisioning-dto";
import {CrudService} from "../services/crud.service";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class ProvisioningService extends CrudService<ProvisioningDto> {

  constructor(http: HttpClient) {
    super(http, "v1/provisioning");
  }

  download(id: number) {
    this.http.get(`${environment.apiPrefix}/provisioning/${id}/download`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }
}
