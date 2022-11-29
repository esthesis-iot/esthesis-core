import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../services/crud.service";
import {environment} from "../../environments/environment";
import {ProvisioningDto} from "./dto/provisioning-dto";

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

  recache(id: string) {
    return this.http.get(`${environment.apiPrefix}/v1/provisioning/${id}/recache`);
  }
}
