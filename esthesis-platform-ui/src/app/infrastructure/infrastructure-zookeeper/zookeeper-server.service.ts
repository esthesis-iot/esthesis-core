import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {ZookeeperServerDto} from '../../dto/zookeeper-server-dto';

@Injectable({
  providedIn: 'root'
})
export class ZookeeperServerService extends CrudService<ZookeeperServerDto> {
  constructor(http: HttpClient) {
    super(http, 'zookeeper-server');
  }
}
