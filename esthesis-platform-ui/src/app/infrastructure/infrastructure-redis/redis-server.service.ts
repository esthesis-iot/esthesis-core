import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {RedisServerDto} from '../../dto/redis-server-dto';

@Injectable({
  providedIn: 'root'
})
export class RedisServerService extends CrudService<RedisServerDto> {
  constructor(http: HttpClient) {
    super(http, 'redis-server');
  }
}
