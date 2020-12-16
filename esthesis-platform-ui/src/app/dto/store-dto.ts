export class StoreDto {
  id: number;
  name: string | undefined;
  password: string | undefined;

  constructor(id: number) {
    this.id = id;
  }
}
