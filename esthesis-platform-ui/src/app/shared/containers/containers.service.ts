import {Injectable} from '@angular/core';
import {CrudService} from '../../services/crud.service';
import {ContainerDto} from '../../dto/container-dto';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ContainersService extends CrudService<ContainerDto> {

  constructor(http: HttpClient) {
    super(http, 'containers');
  }
}
