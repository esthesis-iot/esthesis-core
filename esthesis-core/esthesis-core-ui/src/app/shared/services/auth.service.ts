import {HttpClient} from "@angular/common/http";
import {map} from "rxjs";
import {LogLevel, StsConfigHttpLoader} from "angular-auth-oidc-client";

export const httpLoaderFactory = (httpClient: HttpClient) => {
  const config$ = httpClient.get<any>("api/public-access/oidc-config").pipe(
    map((customConfig: any) => {
      // If authority URL starts with HTTP or HTTPS, we use it as is. In this case, we assume the
      // user has specified a custom STS provider.
      // Otherwise, we prepend the current protocol, host, and port to it. In this case, we assume
      // the embedded STS provider (Keycloak) is used.
      if (!customConfig.authority.startsWith("http") && !customConfig.authority.startsWith("https")) {
        customConfig.authority = window.location.protocol + "//" + window.location.host
          + (window.location.port ? ":" + window.location.port : "")  + customConfig.authority;
      }

      // TODO add in comments
      console.log("Using STS config: " + JSON.stringify(customConfig));

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
