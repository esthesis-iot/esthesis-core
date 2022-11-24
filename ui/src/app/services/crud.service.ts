import {HttpClient, HttpEvent, HttpRequest, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {QPageableReply} from "@qlack/forms";
import {environment} from "../../environments/environment";
import {FormGroup} from "@angular/forms";
import * as fs from "file-saver";

/**
 * A convenience CRUD service to be extended by concrete services to provide default CRUD methods.
 */
export class CrudService<T> {
  constructor(public http: HttpClient, private endpoint: string) {
  }

  save(object: T | T[]): Observable<any> {
    return this.http.post(`${environment.apiPrefix}/${this.endpoint}`, object);
  }

  find(queryString?: string): Observable<QPageableReply<T>> {
    if (queryString) {
      return this.http.get<QPageableReply<T>>(
        `${environment.apiPrefix}/${this.endpoint}/find?${queryString}`);
    } else {
      return this.http.get<QPageableReply<T>>(`${environment.apiPrefix}/${this.endpoint}`);
    }
  }

  findById(id: any): Observable<T> {
    return this.http.get<T>(`${environment.apiPrefix}/${this.endpoint}/${id}`);
  }

  // getAny(): Observable<T> {
  //   return this.http.get<T>(`${environment.apiPrefix}/${this.endpoint}`);
  // }

  delete(id: any): Observable<any> {
    return this.http.delete(`${environment.apiPrefix}/${this.endpoint}/${id}`);
  }

  // deleteAll(): Observable<any> {
  //   return this.http.delete(`${environment.apiPrefix}/${this.endpoint}`);
  // }

  upload(form: FormGroup, url?: string, reportProgress?: boolean): Observable<HttpEvent<{}>> {
    const formData = new FormData();
    for (const formField in form.value) {
      formData.append(formField, form.value[formField]);
    }
    const req = new HttpRequest(
      "POST",
      url ? url : `${environment.apiPrefix}/${this.endpoint}`,
      formData, {
        reportProgress: reportProgress
      }
    );

    return this.http.request(req);

    // @ts-ignore
    return null;
  }

  saveAs(onNext: HttpResponse<Blob>) {
    const blob = new Blob([onNext.body!], {type: "application/octet-stream"});
    const filename = onNext.headers.get("Content-Disposition")!.split(";")[1].split("=")[1];
    fs.saveAs(blob, filename);
  }
}
