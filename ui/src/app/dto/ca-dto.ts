export class CaDto {
  id: string;
  cn: string;
  parentCa: string | undefined;
  parentCaId: string | undefined;
  issued: Date | undefined;
  validity: Date | undefined;
  publicKey: File | undefined;
  certificate: string | undefined;

  constructor(id: string, cn: string) {
    this.id = id;
    this.cn = cn;
  }
}
