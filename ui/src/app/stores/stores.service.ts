import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {StoreDto} from "./dto/store-dto";
import {environment} from "../../environments/environment";
import {CrudDownloadService} from "../services/crud-download.service";
import {FileSaverService} from "ngx-filesaver";

@Injectable({
  providedIn: "root"
})
export class StoresService extends CrudDownloadService<StoreDto> {
  private prefix = environment.apiPrefix + "/store/v1";

  constructor(http: HttpClient, fs: FileSaverService) {
    super(http, "store/v1", fs);
  }

  download(storeId: string) {
    this.http.get(
      `${this.prefix}/${storeId}/download`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }
}
