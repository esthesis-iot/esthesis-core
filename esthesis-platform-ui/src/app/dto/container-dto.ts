import {ContainerPortDto} from './container-port-dto';
import {ContainerVolumeDto} from './container-volume-dto';
import {ContainerEnvDto} from './container-env-dto';

export class ContainerDto {
  container: string;
  registryUsername: string;
  registryPassword: string;
  network: string;
  restart: string;
  ports: ContainerPortDto[];
  volumes: ContainerVolumeDto[];
  env: ContainerEnvDto[];
  scale: number;
}
