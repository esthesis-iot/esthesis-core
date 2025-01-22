import {Injectable} from "@angular/core";
import {AbstractControl, FormGroup} from "@angular/forms";

@Injectable({
  providedIn: "root"
})
export class QFormValidationEEService {

  private static readonly FORM_TYPE = {
    CLASSIC: "CLASSIC",
    FORMLY: "FORMLY"
  };

  private invalidateFormControl(form: FormGroup, validationError: ValidationError, formType: string) {
    let erroneousField = validationError.field;
    let fieldFormControl: AbstractControl;
    erroneousField = erroneousField.substring(erroneousField.lastIndexOf(".") + 1);
    fieldFormControl = form.controls[erroneousField];
    if (!fieldFormControl) {
      console.error("Could not find field '" + erroneousField + "'to set validation error.");
    } else {
      console.debug("Setting validation error on field '" + erroneousField + "' with message: " + validationError.message);
      if (formType === QFormValidationEEService.FORM_TYPE.CLASSIC) {
        fieldFormControl.setErrors({
          incorrect: true,
          message: validationError.message,
        });
      } else {
        fieldFormControl.setErrors({
          serverError:{
            message: validationError.message
          }
        });
      }
    }
  }

  public applyValidationErrors(form: FormGroup, validationErrors: ValidationError[]) {
    validationErrors.forEach((validationError: ValidationError) => {
      this.invalidateFormControl(form, validationError, QFormValidationEEService.FORM_TYPE.CLASSIC);
    });
    form.markAllAsTouched();
  }

  public applyFormlyValidationErrors(form: FormGroup, validationErrors: ValidationError[]) {
    validationErrors.forEach((validationError: ValidationError) => {
      this.invalidateFormControl(form, validationError, QFormValidationEEService.FORM_TYPE.FORMLY);
    });

    form.markAllAsTouched();
  }
}

export interface ValidationError {
  field: string;
  message: string;
}
