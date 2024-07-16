export class ConstraintViolationDto {
  constructor(field: string, message: string) {
    this.field = field;
    this.message = message;
  }

  field: string;
  message: string;
}
