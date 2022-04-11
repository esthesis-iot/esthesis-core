export class SettingDto {
  key: string;
  val: any;

  constructor(key: string, val: any) {
    this.key = key;
    this.val = val;
  }
}
