import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {QFormsService} from '@eurodyn/forms';
import {ContainerDto} from '../../../dto/container-dto';
import {CrudService} from '../../../services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class ContainersService extends CrudService<ContainerDto> {

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'containers', qForms);
  }
}
