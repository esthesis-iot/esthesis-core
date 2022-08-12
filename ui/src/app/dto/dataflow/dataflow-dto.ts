export class DataflowDto {
  id!: string;
  name!: string;
  description?: string;
  status!: boolean;
  type!: string;
  minPods!: number;
  maxPods!: number;
  inChannel?: string;
  outChannel?: string;
  consumerGroup?: string;

  configuration?: string;
}
