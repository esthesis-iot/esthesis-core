import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {ProvisioningDto} from "./dto/provisioning-dto";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";

@Injectable({
  providedIn: "root"
})
export class ProvisioningService extends CrudDownloadService<ProvisioningDto> {

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "v1/provisioning", fs);
  }

  download(id: string) {
    this.http.get(`${environment.apiPrefix}/v1/provisioning/${id}/download`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  recache(id: string) {
    return this.http.get(`${environment.apiPrefix}/v1/provisioning/${id}/recache`);
  }

  recacheAll() {
    return this.http.get(`${environment.apiPrefix}/v1/provisioning/recache`);
  }

}
