export class GroupProgressDto {
  name!: string;
  progress!: number;

  constructor(name: string, progress: number) {
    this.name = name;
    this.progress = progress;
  }
}
