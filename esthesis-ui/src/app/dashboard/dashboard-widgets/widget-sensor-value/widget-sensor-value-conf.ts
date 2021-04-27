export class WidgetSensorValueConf {

  title: string;
  unit: string;
  icon: string;
  hardwareId: string;
  measurement: string;
  bgColor: string;
  fgColor: string;
  displayNodeName: boolean;

  constructor(title: string, icon: string, hardwareId: string, measurement: string,
              bgColor: string, fgColor: string, unit: string, displayNodeName: boolean) {
    this.title = title;
    this.icon = icon;
    this.hardwareId = hardwareId;
    this.measurement = measurement;
    this.bgColor = bgColor;
    this.fgColor = fgColor;
    this.unit = unit;
    this.displayNodeName = displayNodeName;
  }

  serialise(): string {
    return JSON.stringify(this);
  }

  static deserialise(conf: string): WidgetSensorValueConf {
    return JSON.parse(conf);
  }
}
