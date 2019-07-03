export class CaDto {
  id: number;
  cn: string;
  parentCa: number;
  issued: Date;
  validity: Date;
  publicKey: File;
  certificate: string;

  constructor(id: number, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
