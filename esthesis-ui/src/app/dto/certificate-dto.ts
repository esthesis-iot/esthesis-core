export class CertificateDto {
  id: number;
  cn: string;
  issued: Date;
  validity: Date;
  certificate: string;
  publicKey: string;
  privateKey: string;
  issuer: string;

  constructor(id: number, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
