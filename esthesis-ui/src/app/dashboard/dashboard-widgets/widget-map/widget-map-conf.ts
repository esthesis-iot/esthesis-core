export class WidgetMapConf {

  title: string;
  bgColor: string;
  fgColor: string;
  hardwareIds: string;
  tags: string;

  constructor(title: string, bgColor: string, fgColor: string, hardwareIds: string, tags: string) {
    this.title = title;
    this.bgColor = bgColor;
    this.fgColor = fgColor;
    this.hardwareIds = hardwareIds;
    this.tags = tags;
  }

  serialise(): string {
    return JSON.stringify(this);
  }

  static deserialise(conf: string): WidgetMapConf {
    return JSON.parse(conf);
  }
}
