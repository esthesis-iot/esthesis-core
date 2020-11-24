import {Component, OnInit} from '@angular/core';
import {AboutService} from './about.service';
import {AboutDto} from '../dto/about-dto';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss']
})
export class AboutComponent implements OnInit {
  about: AboutDto;

  constructor(private aboutService: AboutService) {
  }

  ngOnInit() {
    this.aboutService.getAbout().subscribe(onNext => {
      this.about = onNext;
    });
  }

}
