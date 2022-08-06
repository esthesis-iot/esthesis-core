import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../services/crud.service";
import {StoreDto} from "../dto/store-dto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class StoresService extends CrudService<StoreDto> {

  constructor(http: HttpClient) {
    super(http, "v1/store");
  }

  download(storeId: string) {
    this.http.get(
      `${environment.apiPrefix}/v1/store/${storeId}/download`, {
        responseType: "blob", observe: "response"
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }
}
