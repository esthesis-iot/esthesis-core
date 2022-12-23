import {Injectable} from "@angular/core";
import {AbstractControl, FormGroup} from "@angular/forms";

@Injectable({
  providedIn: "root"
})
export class QFormValidationEEService {

  public validateForm(theForm: FormGroup, validationErrors: any) {
    validationErrors.forEach((validationError: any) => {
      this.invalidateFormControl(theForm, validationError);
    });
  }

  private invalidateFormControl(theForm: FormGroup, validationError: any) {
    let erroneousField = validationError.field;
    let fieldFormControl: AbstractControl;
    erroneousField = erroneousField.substring(erroneousField.lastIndexOf(".") + 1);
    fieldFormControl = theForm.controls[erroneousField];
    fieldFormControl.setErrors({
      incorrect: true,
      message: validationError.message,
    });
  }

}
