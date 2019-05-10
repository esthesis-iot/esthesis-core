import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {LoginInfoDto} from '../dto/login-info-dto';
import {UserDto} from '../dto/user-dto';
import {UserProfileDto} from '../dto/user-profile-dto';
import {CrudService} from '../services/crud.service';
import {QFormsService} from '@eurodyn/forms';

/**
 * A service providing functionality for the user of the application, including authentication,
 * authorisation and session management.
 */
@Injectable({
  providedIn: 'root'
})
export class UserService extends CrudService<UserDto> {
  private resource = `users`;

  constructor(http: HttpClient, private jwtService: JwtHelperService, qForms: QFormsService) {
    super(http, 'users', qForms);
  }

  // Returns the JWT.
  private static getJwt(): string {
    return localStorage.getItem(AppConstants.JWT_STORAGE_NAME);
  }

  // Authenticate a user.
  // TODO observable type
  login(loginInfoDTO: LoginInfoDto): Observable<string> {
    return this.http.post<string>(AppConstants.API_ROOT + `/${this.resource}/auth`, JSON.stringify(loginInfoDTO),
      {headers: {'Content-Type': 'application/json'}});
  }

  // Return a claim from JWT.
  getJWTClaim(claim: string): string {
    let claimValue: string;

    if (UserService.getJwt()) {
      claimValue = this.jwtService.decodeToken(UserService.getJwt())[claim];
    }

    return claimValue;
  }

  // Logs out the user terminating its session.
  // TODO observable type
  logout(): Observable<any> {
    return this.http.get(AppConstants.API_ROOT + `/${this.resource}/logout`);
  }

  // Get the profile of the currently registered user.
  getUserProfile(): Observable<UserProfileDto> {
    return this.http.get<UserProfileDto>(AppConstants.API_ROOT + `/${this.resource}`);
  }

  // // Update the user profile.
  // // TODO observable type
  // updateUserProfile(userProfileDTO: UserProfileDto) {
  //   return this.localHttp.put(AppConstants.API_ROOT + `/${this.resource}`, JSON.stringify(userProfileDTO),
  //     {headers: {'Content-Type': 'application/json'}});
  // }

  // Update the user profile.
  // TODO observable type
  updateUserProfile(object: UserProfileDto): Observable<any> {
    return this.http.put(AppConstants.API_ROOT + `/${this.resource}`, object);
  }

  getUserRoles(): Observable<any> {
    return this.http.get(AppConstants.API_ROOT + `/${this.resource}` + '/types');
  }

  getUserStatus(): Observable<any> {
    return this.http.get(AppConstants.API_ROOT + `/${this.resource}` + '/status');
  }
}
