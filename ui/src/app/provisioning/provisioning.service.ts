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
  private prefix = environment.apiPrefix + "/provisioning/v1";

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

  recache(id: string) {
    return this.http.get(`${this.prefix}/${id}/recache`);
  }

  recacheAll() {
    return this.http.get(`${this.prefix}/recache`);
  }

  findBaseVersions(tags: string) {
    return this.http.get(`${this.prefix}/find/by-tags?tags=${tags}`);
  }
}
