import {HttpClient} from "@angular/common/http";
import {map} from "rxjs";
import {LogLevel, StsConfigHttpLoader} from "angular-auth-oidc-client";

export const httpLoaderFactory = (httpClient: HttpClient) => {
  const config$ = httpClient.get<any>("api/public-access/oidc-config").pipe(
    map((customConfig: any) => {
      return {
        authority: customConfig.authority,
        redirectUrl: customConfig.redirectUrl,
        postLogoutRedirectUri: customConfig.postLogoutRedirectUri,
        clientId: customConfig.clientId,
        scope: customConfig.scope,
        responseType: customConfig.responseType,
        silentRenew: customConfig.silentRenew,
        useRefreshToken: customConfig.useRefreshToken,
        renewTimeBeforeTokenExpiresInSeconds: customConfig.renewTimeBeforeTokenExpiresInSeconds,
        maxIdTokenIatOffsetAllowedInSeconds: customConfig.maxIdTokenIatOffsetAllowedInSeconds,
        ignoreNonceAfterRefresh: customConfig.ignoreNonceAfterRefresh,
        secureRoutes: customConfig.secureRoutes,
        logLevel: LogLevel.Warn
      };
    })
  );

  return new StsConfigHttpLoader(config$);
};
