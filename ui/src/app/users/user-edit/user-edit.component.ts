import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../user.service";
import {BaseComponent} from "../../shared/components/base-component";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {UserDto} from "../dto/user-dto";
import {AppConstants} from "../../app.constants";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-user",
  templateUrl: "./user-edit.component.html",
  styleUrls: ["./user-edit.component.scss"]
})
export class UserEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id: string | null | undefined;
  hide1 = true;
  hide2 = true;
  isEdit = false;
  // Expose application constants.
  constants = AppConstants;

  constructor(private fb: FormBuilder, private userService: UserService,
    private route: ActivatedRoute,
    private qForms: QFormsService, private router: Router, private dialog: MatDialog,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id");
    this.isEdit = (this.id !== "0");

    // Setup the form.
    this.form = this.fb.group({
      id: ["0"],
      username: [{value: "", disabled: this.isEdit}, [Validators.required]],
      status: [{value: "", disabled: false}, [Validators.required, Validators.maxLength(1024)]],
      newPassword1: [{value: "", disabled: false}, [Validators.maxLength(1024)]],
      newPassword2: [{value: "", disabled: false}, [Validators.maxLength(1024)]],
      password: []
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== "0") {
      this.userService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  save() {
    if (this.form.controls.newPassword1.value === this.form.controls.newPassword2.value) {
      this.form.controls.password.setValue(this.form.controls.newPassword1.value);
      this.form.controls.newPassword1.setValue(null);
      this.form.controls.newPassword2.setValue(null);
    }
    this.userService.save(this.form.getRawValue() as UserDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess(this.isEdit ? "User was successfully edited."
          : "User was successfully created.");
        this.router.navigate(["users"]);
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete User",
        question: "Do you really want to delete this user?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.userService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("User successfully deleted.");
            this.router.navigate(["users"]);
          }
        });
      }
    });
  }
}
