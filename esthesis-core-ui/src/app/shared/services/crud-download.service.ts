import {CrudService} from "./crud.service";
import { HttpClient, HttpResponse } from "@angular/common/http";
import {FileSaverService} from "ngx-filesaver";

export class CrudDownloadService<T> extends CrudService<T> {
  constructor(http: HttpClient, endpoint: string, private fs: FileSaverService) {
    super(http, endpoint);
  }

  saveAs(onNext: HttpResponse<Blob>) {
    const blob = new Blob([onNext.body!], {type: "application/octet-stream"});
    let filename = onNext.headers.get("Content-Disposition")!.split(";")[1].split("=")[1];
    if (filename.startsWith("\"")) {
      filename = filename.substring(1, filename.length - 1);
    }
    if (filename.endsWith("\"")) {
      filename = filename.substring(0, filename.length - 1);
    }
    this.fs.save(blob, filename);
  }
}
