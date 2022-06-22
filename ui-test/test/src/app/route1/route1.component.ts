import {HttpClient} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {environment} from "../../environments/environment";
import {OidcSecurityService} from "angular-auth-oidc-client";

@Component({
  selector: 'app-route1',
  templateUrl: './route1.component.html',
  styleUrls: ['./route1.component.scss']
})
export class Route1Component implements OnInit {
  accessToken?: String;
  idToken?: String;
  refreshToken?: String;
  userData?: String;

  constructor(private httpClient: HttpClient, private oidcService: OidcSecurityService) {
  }

  ngOnInit(): void {
    this.oidcService.getAccessToken().subscribe(
      (data) => {
        this.accessToken = data;
      }
    );
    this.oidcService.getIdToken().subscribe((data) => {
        this.idToken = data;
      }
    );
    this.oidcService.getRefreshToken().subscribe((data) => {
        this.refreshToken = data;
      }
    );
    this.oidcService.getUserData().subscribe((data) => {
        this.userData = data;
      }
    );
  }

  test1() {
    this.httpClient.get(environment.apiPrefix + "/v1/device").subscribe(
      (data) => {
        console.log(data);
      }
    );
  }

  test2() {
    this.httpClient.get(environment.apiPrefix + "/v1/conf-public").subscribe(
      (data) => {
        console.log(data);
      }
    );
  }

  refresh() {

  }
}
