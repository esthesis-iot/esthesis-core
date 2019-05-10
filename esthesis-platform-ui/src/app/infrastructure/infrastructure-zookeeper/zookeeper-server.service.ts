import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {ZookeeperServerDto} from '../../dto/zookeeper-server-dto';
import {QFormsService} from '@eurodyn/forms';

@Injectable({
  providedIn: 'root'
})
export class ZookeeperServerService extends CrudService<ZookeeperServerDto> {
  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'zookeeper-server', qForms);
  }
}
