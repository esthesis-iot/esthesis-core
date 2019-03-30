export class TunnelServerDto {
  name: string;
  ip: string;
  publicKey: string;
  privateKey: string;
  certificate: string;
  status: boolean
  token: string;
  validity: Date;
  parentCa: string;
}
