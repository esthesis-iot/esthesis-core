import { Component, OnInit } from '@angular/core';
import { semver } from './git-version.json';
import { raw } from './git-version.json';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.scss']
})
export class AboutComponent implements OnInit {
  private semver = semver;
  private raw = raw;

  constructor() { }

  ngOnInit() {
  }

}
