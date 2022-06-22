import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ConfigDto} from "../dto/config-dto";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  constructor(private httpClient: HttpClient) { }

  config() {
    return new Observable((subscriber) => {
      this.httpClient.get<ConfigDto>('https://jsonplaceholder.typicode.com/users/1').subscribe(res => {
        console.log(res);
        subscriber.complete();
      }, error => {
        console.log(error);
        subscriber.error();
      })
    });
  }
}3
