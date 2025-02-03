import {HttpClient, HttpEvent, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {QPageableReply} from "@qlack/forms";
import {AppConstants} from "../../app.constants";

/**
 * A convenience CRUD service to be extended by concrete services to provide default CRUD methods.
 */
export class CrudService<T> {
  constructor(public http: HttpClient, private readonly endpoint: string) {
  }

  save(object: T | T[]): Observable<any> {
    return this.http.post(`${AppConstants.API_ROOT}/${this.endpoint}`, object);
  }

  find(queryString?: string): Observable<QPageableReply<T>> {
    if (queryString) {
      return this.http.get<QPageableReply<T>>(
        `${AppConstants.API_ROOT}/${this.endpoint}/find?${queryString}`);
    } else {
      return this.http.get<QPageableReply<T>>(`${AppConstants.API_ROOT}/${this.endpoint}`);
    }
  }

  findById(id: any): Observable<T> {
    return this.http.get<T>(`${AppConstants.API_ROOT}/${this.endpoint}/${id}`);
  }

  findByIds(id: string[]): Observable<T[]> {
    return this.http.get<T[]>(`${AppConstants.API_ROOT}/${this.endpoint}/find/by-ids?ids=${id}`);
  }

  delete(id: any): Observable<any> {
    return this.http.delete(`${AppConstants.API_ROOT}/${this.endpoint}/${id}`);
  }

  /**
   * Uploads an object with files.
   * @param object The object to upload.
   * @param files A map of files to upload. The key is the form field name and the value is the
   *   file.
   * @param url The URL to upload to. If not provided, the default endpoint URL will be used.
   */
  upload(object: any, files: Map<string, File | null>, url?: string): Observable<HttpEvent<{}>> {
    if (!url) {
      url = `${AppConstants.API_ROOT}/${this.endpoint}`;
    }

    const formData: FormData = new FormData();
    formData.append('dto', JSON.stringify(object));
    if (files && files.size > 0) {
      files.forEach((value, key) => {
        if (value) {
          formData.append(key, value, value.name);
        }
      });
    }
    const req = new HttpRequest("POST", url, formData, {
        reportProgress: true
      }
    );

    return this.http.request(req);
  }

}
