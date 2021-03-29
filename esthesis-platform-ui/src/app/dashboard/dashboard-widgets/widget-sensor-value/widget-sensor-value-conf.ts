export class WidgetSensorValueConf {

  title!: string;
  icon!: string;
  hardwareId!: string;
  measurement!: string;

  constructor(title: string, icon: string, hardwareId: string, measurement: string) {
    this.title = title;
    this.icon = icon;
    this.hardwareId = hardwareId;
    this.measurement = measurement;
  }

  serialise(): string {
    return JSON.stringify(this);
  }

  static deserialise(conf: string): WidgetSensorValueConf {
    return JSON.parse(conf);
  }
}
