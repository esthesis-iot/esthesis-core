export class CertificateDto {
  id: number;
  cn: string;
  issued: Date | undefined;
  validity: Date | undefined;
  certificate: string | undefined;
  publicKey: string | undefined;
  privateKey: string | undefined;
  issuer: string | undefined;

  constructor(id: number, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
