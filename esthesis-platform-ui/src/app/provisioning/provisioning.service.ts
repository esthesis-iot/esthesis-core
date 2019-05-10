import {Injectable} from '@angular/core';
import {AppConstants} from '../app.constants';
import {HttpClient} from '@angular/common/http';
import {ProvisioningDto} from '../dto/provisioning-dto';
import {FormGroup} from '@angular/forms';
import {HttpUtilsService} from '../shared/http-utils.service';
import {QFormsService} from '@eurodyn/forms';
import {CrudService} from '../services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class ProvisioningService extends CrudService<ProvisioningDto> {

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'provisioning', qForms);
  }

  // save(form: FormGroup) {
  //   return this.qForms.uploadForm(this.localHttp, form, `${AppConstants.API_ROOT}/provisioning`, false);
  // }
}
