export class WidgetSensorValueConf {

  title: string;
  icon: string;
  hardwareId: string;
  measurement: string;
  bgColor: string;
  fgColor: string;

  constructor(title: string, icon: string, hardwareId: string, measurement: string, bgColor: string,  fgColor: string) {
    this.title = title;
    this.icon = icon;
    this.hardwareId = hardwareId;
    this.measurement = measurement;
    this.bgColor = bgColor;
    this.fgColor = fgColor;
  }

  serialise(): string {
    return JSON.stringify(this);
  }

  static deserialise(conf: string): WidgetSensorValueConf {
    return JSON.parse(conf);
  }
}
