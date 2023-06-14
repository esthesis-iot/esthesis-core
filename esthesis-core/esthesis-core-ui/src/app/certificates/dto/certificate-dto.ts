export class CertificateDto {
  id: string;
  cn: string;
  name?: string;
  issued: Date | undefined;
  validity: Date | undefined;
  certificate: string | undefined;
  publicKey: string | undefined;
  privateKey: string | undefined;
  issuer: string | undefined;

  constructor(id: string, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
