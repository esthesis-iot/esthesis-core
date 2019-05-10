import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {ContainerDto} from '../../dto/container-dto';
import {HttpClient} from '@angular/common/http';
import {QFormsService} from '@eurodyn/forms';

@Injectable({
  providedIn: 'root'
})
export class ContainersService extends CrudService<ContainerDto> {

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'containers', qForms);
  }
}
