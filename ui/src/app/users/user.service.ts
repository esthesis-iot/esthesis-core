import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {LoginInfoDto} from '../dto/login-info-dto';
import {UserDto} from '../dto/user-dto';
import {CrudService} from '../services/crud.service';
import {JwtDto} from '../dto/jwt-dto';
import {environment} from "../../environments/environment";

/**
 * A service providing functionality for the user of the application, including authentication,
 * authorisation and session management.
 */
@Injectable({
  providedIn: 'root'
})
export class UserService extends CrudService<UserDto> {
  private resource = `users`;

  constructor(http: HttpClient) {
    super(http, 'users');
  }

  // Returns the JWT.
  private static getJwt(): string | null {
    // return localStorage.getItem(AppConstants.JWT_STORAGE_NAME);
    return null;
  }

  // Authenticate a user.`
  login(loginInfoDTO: LoginInfoDto): Observable<JwtDto> {
    return this.http.post<JwtDto>(environment.apiPrefix + `/${this.resource}/auth`,
      JSON.stringify(loginInfoDTO),
      {headers: {'Content-Type': 'application/json'}});
  }

  // Return a claim from JWT.
  getJWTClaim(claim: string): string | null {
    return null;
    // if (UserService.getJwt()) {
    //   // @ts-ignore
    //   return this.jwtService.decodeToken(UserService.getJwt())[claim];
    // } else {
    //   return null;
    // }
  }

  // Logs out the user terminating its session.
  logout(): Observable<any> {
    return this.http.get(environment.apiPrefix + `/${this.resource}/logout`);
  }

  // Save user
  save(user: UserDto) {
    return this.http.post(environment.apiPrefix + `/${this.resource}`, JSON.stringify(user),
      {headers: {'Content-Type': 'application/json'}});
  }

}
