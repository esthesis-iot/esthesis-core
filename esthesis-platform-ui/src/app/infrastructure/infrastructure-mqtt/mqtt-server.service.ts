import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MqttServerDto} from '../../dto/mqtt-server-dto';
import {CrudService} from '../../services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class MqttServerService extends CrudService<MqttServerDto> {
  constructor(http: HttpClient) {
    super(http, 'mqtt-server');
  }
}
