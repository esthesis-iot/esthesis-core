import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {QFormsService} from '@eurodyn/forms';
import {CrudService} from '../services/crud.service';
import {StoreDto} from '../dto/store-dto';
import {AppConstants} from '../app.constants';
import {HttpUtilsService} from '../shared/service/http-utils.service';

@Injectable({
  providedIn: 'root'
})
export class StoresService extends CrudService<StoreDto> {

  constructor(http: HttpClient, qForms: QFormsService, private httpUtil: HttpUtilsService) {
    super(http, 'stores', qForms);
  }

  download(storeId: number) {
    this.http.get(
      `${AppConstants.API_ROOT}/stores/${storeId}/download`, {
        responseType: 'blob', observe: 'response'
      }).subscribe(onNext => {
      this.httpUtil.saveAs(onNext);
    });
  }
}
