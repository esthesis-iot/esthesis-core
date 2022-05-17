export class WidgetSensorGaugeConf {

  title: string;
  hardwareId: string;
  measurement: string;
  bgColor: string;
  fgColor: string;
  unit: string;
  min: number;
  max: number;
  bigSegments: number;
  smallSegments: number;
  showAxis: boolean;


  constructor(title: string, hardwareId: string, measurement: string, bgColor: string,
              fgColor: string, unit: string, min: number, max: number, bigSegments: number,
              smallSegments: number, showAxis: boolean) {
    this.title = title;
    this.hardwareId = hardwareId;
    this.measurement = measurement;
    this.bgColor = bgColor;
    this.fgColor = fgColor;
    this.unit = unit;
    this.min = min;
    this.max = max;
    this.bigSegments = bigSegments;
    this.smallSegments = smallSegments;
    this.showAxis = showAxis;
  }

  serialise(): string {
    return JSON.stringify(this);
  }

  static deserialise(conf: string): WidgetSensorGaugeConf {
    return JSON.parse(conf);
  }
}
