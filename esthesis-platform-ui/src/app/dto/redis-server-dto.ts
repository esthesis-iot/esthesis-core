export class RedisServerDto {
  id: string;
  name: string;
  ipAddress: string;
  state: boolean;
  username: string;
  password: string;
  caCert: string;
  clientCert: string;
  clientKey: string;
}
