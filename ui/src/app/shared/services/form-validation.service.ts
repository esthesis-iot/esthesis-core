import {Injectable} from "@angular/core";
import {AbstractControl, FormGroup} from "@angular/forms";

@Injectable({
  providedIn: "root"
})
export class QFormValidationEEService {

  private invalidateFormControl(form: FormGroup, validationError: ValidationError) {
    let erroneousField = validationError.field;
    let fieldFormControl: AbstractControl;
    erroneousField = erroneousField.substring(erroneousField.lastIndexOf(".") + 1);
    fieldFormControl = form.controls[erroneousField];
    fieldFormControl.setErrors({
      incorrect: true,
      message: validationError.message,
    });
  }

  public applyValidationErrors(form: FormGroup, validationErrors: ValidationError[]) {
    validationErrors.forEach((validationError: ValidationError) => {
      this.invalidateFormControl(form, validationError);
    });

    form.markAllAsTouched();
  }
}

export interface ValidationError {
  field: string;
  message: string;
}
