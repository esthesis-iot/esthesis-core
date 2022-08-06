export class StoreDto {
  id: string;
  name: string | undefined;
  password: string | undefined;

  constructor(id: string) {
    this.id = id;
  }
}
