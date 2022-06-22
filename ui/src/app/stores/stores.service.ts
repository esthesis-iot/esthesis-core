import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CrudService} from '../services/crud.service';
import {StoreDto} from '../dto/store-dto';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class StoresService extends CrudService<StoreDto> {

  constructor(http: HttpClient) {
    super(http, 'stores');
  }

  download(storeId: number) {
    this.http.get(
      `${environment.apiPrefix}/stores/${storeId}/download`, {
        responseType: 'blob', observe: 'response'
      }).subscribe(onNext => {
      this.saveAs(onNext);
    });
  }
}
