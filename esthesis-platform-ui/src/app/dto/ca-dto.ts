export class CaDto {
  id: number;
  cn: string;
  parentCa: number;
  issued: Date;
  validity: Date;
  publicKey: File;
}
