import {HttpClient} from "@angular/common/http";
import {map} from "rxjs";
import {LogLevel, StsConfigHttpLoader} from "angular-auth-oidc-client";
import {OidcConfigDto} from "./dto/oidc-config-dto";

export const httpLoaderFactory = (httpClient: HttpClient) => {
  const config$ = httpClient.get<OidcConfigDto>("api/public-access/oidc-config").pipe(
    map((config: any) => {
      return {
        authority: config.authority,
        redirectUrl: config.redirectUrl,
        postLogoutRedirectUri: config.postLogoutRedirectUri,
        clientId: config.clientId,
        scope: config.scope,
        responseType: config.responseType,
        silentRenew: config.silentRenew,
        useRefreshToken: config.useRefreshToken,
        renewTimeBeforeTokenExpiresInSeconds: config.renewTimeBeforeTokenExpiresInSeconds,
        maxIdTokenIatOffsetAllowedInSeconds: config.maxIdTokenIatOffsetAllowedInSeconds,
        ignoreNonceAfterRefresh: config.ignoreNonceAfterRefresh,
        secureRoutes: config.secureRoutes,
        logLevel: LogLevel.Warn,
      };
    })
  );

  return new StsConfigHttpLoader(config$);
};
