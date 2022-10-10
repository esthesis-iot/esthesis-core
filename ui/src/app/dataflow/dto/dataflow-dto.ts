export class DataflowDto {
  id!: string;
  name!: string;
  description?: string;
  status!: boolean;
  type!: string;
  minPods!: number;
  maxPods!: number;
  namespace!: string;
  version!: string;

  configuration?: string;
}
