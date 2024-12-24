import {Injectable} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {ProvisioningDto} from "./dto/provisioning-dto";
import {CrudDownloadService} from "../shared/services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class ProvisioningService extends CrudDownloadService<ProvisioningDto> {
  private prefix = AppConstants.API_ROOT + "/provisioning/v1";

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "provisioning/v1", fs);
  }

  download(id: string) {
    this.http.get(`${this.prefix}/${id}/download`, {
      responseType: "blob", observe: "response"
    }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }

  recacheAll() {
    return this.http.get(`${this.prefix}/recache`);
  }

  findBaseVersions(tags: string) {
    return this.http.get(`${this.prefix}/find/by-tags?tags=${tags}`);
  }
}
