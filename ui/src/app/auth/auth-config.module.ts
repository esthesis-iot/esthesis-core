import {NgModule} from '@angular/core';
import {AuthModule, LogLevel} from 'angular-auth-oidc-client';


@NgModule({
  imports: [AuthModule.forRoot({
    config: {
      //TODO make this configurable
      authority: 'http://esthesis-dev-keycloak/realms/esthesis',
      redirectUrl: window.location.origin,
      postLogoutRedirectUri: window.location.origin,
      clientId: 'esthesis',
      scope: 'openid profile offline_access',
      responseType: 'code',
      silentRenew: true,
      useRefreshToken: true,
      renewTimeBeforeTokenExpiresInSeconds: 30,
      maxIdTokenIatOffsetAllowedInSeconds: 10,
      ignoreNonceAfterRefresh: true,
      secureRoutes: ["/dev", "/api"],
      logLevel: LogLevel.Warn
    }
  })],
  exports: [AuthModule],
})
export class AuthConfigModule {
}
