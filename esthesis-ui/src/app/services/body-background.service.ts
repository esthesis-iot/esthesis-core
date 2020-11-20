import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import 'rxjs-compat/add/operator/map';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BodyBackgroundService {
  private imageSample = 10;
  constructor(private httpClient: HttpClient) {
  }

  public getImageUrl(): Observable<string> {
    // TODO check if number of returned images is < imageSample and adapt accordingly.
    // TODO externalise API key in settings.
    // TODO allow categories to be defined in settings.
    return this.httpClient.get(
      // tslint:disable-next-line:max-line-length
      'https://pixabay.com/api/?key=11450182-74bb65cfe11d586f9a5fc8846&image_type=photo&orientation=horizontal&category=nature&safesearch=true&per_page='
      + this.imageSample + '&order=latest')
    .map(value => {
        return value['hits'][Math.floor(Math.random() * this.imageSample)]['largeImageURL']; // NOSONAR
      });
  }
}
