import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {HttpClient} from '@angular/common/http';
import {ProvisioningDto} from '../dto/provisioning-dto';
import {QFormsService} from '@eurodyn/forms';
import {CrudService} from '../services/crud.service';
import {HttpUtilsService} from '../shared/service/http-utils.service';

@Injectable({
  providedIn: 'root'
})
export class ProvisioningService extends CrudService<ProvisioningDto> {

  constructor(http: HttpClient, qForms: QFormsService, private httpUtil: HttpUtilsService) {
    super(http, 'provisioning', qForms);
  }

  download(id: number) {
    this.http.get(`${AppConstants.API_ROOT}/provisioning/${id}/download`, {
      responseType: 'blob', observe: 'response'
    }).subscribe(onNext => {
      this.httpUtil.saveAs(onNext);
    });
  }
}
