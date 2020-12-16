export class CaDto {
  id: number;
  cn: string;
  parentCa: number | undefined;
  issued: Date | undefined;
  validity: Date | undefined;
  publicKey: File | undefined;
  certificate: string | undefined;

  constructor(id: number, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
