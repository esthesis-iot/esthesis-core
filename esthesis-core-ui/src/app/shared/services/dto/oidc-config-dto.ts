export interface OidcConfigDto {
  authority: string;
  redirectUrl: string;
  postLogoutRedirectUri: string;
  clientId: string;
  scope: string;
  responseType: string;
  silentRenew: boolean;
  useRefreshToken: boolean;
  renewTimeBeforeTokenExpiresInSeconds: number;
  maxIdTokenIatOffsetAllowedInSeconds: number;
  ignoreNonceAfterRefresh: boolean;
  secureRoutes: string[];
}
