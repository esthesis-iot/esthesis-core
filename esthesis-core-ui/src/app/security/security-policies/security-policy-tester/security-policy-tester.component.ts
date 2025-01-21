import {Component, OnInit, Optional} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatDialogRef} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SecurityService} from "../../security.service";
import {UtilityService} from "../../../shared/services/utility.service";

@Component({
  selector: "app-security-policy-tester",
  templateUrl: "./security-policy-tester.component.html"
})
export class SecurityPolicyTesterComponent implements OnInit {
  form: FormGroup;
  checkStatus = false;

  constructor(private readonly fb: FormBuilder,
    @Optional() private readonly dialogRef: MatDialogRef<SecurityPolicyTesterComponent>,
    private readonly securityService: SecurityService,
    private readonly utilityService: UtilityService) {
    this.form = this.fb.group({
      policy: []
    });
  }

  close() {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.form.controls["policy"].valueChanges.pipe(debounceTime(250), distinctUntilChanged()).subscribe(() => {
      this.securityService.isIncluded(this.form.controls["policy"].value).subscribe({
        next: (result) => {
          this.checkStatus = result;
        }, error: () => {
          this.utilityService.popupError("Could not evaluate policy");
        }
      });
    });
  }
}
