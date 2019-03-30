import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material';
import {FormValidationExtras} from '../shared/form-validation-extras';
import {Observable} from 'rxjs';
import {UserProfileDto} from '../dto/user-profile-dto';
import {UserService} from '../users/user.service';

@Component({
  selector: 'app-profile-details',
  templateUrl: './profile-details.component.html',
  styleUrls: ['./profile-details.component.scss']
})
export class ProfileDetailsComponent implements OnInit {
  // Profile form.
  profileForm: FormGroup;

  constructor(public router: Router, public userService: UserService, private fb: FormBuilder,
              private snackBar: MatSnackBar) {
    this.profileForm = this.fb.group({
      fn: ['', null],
      ln: ['', null],
      email: ['', [Validators.required, Validators.email]],
      oldPassword: ['', null],
      newPassword1: ['', null],
      newPassword2: ['', null],
      newEmail: ['', FormValidationExtras.MatchOptionalEmail]
    }, {
      validator: FormValidationExtras.MatchPassword
    });
  }

  private getFormData(): Observable<UserProfileDto> {
    return this.userService.getUserProfile();
  }

  ngOnInit(): void {
    this.getFormData().subscribe(onNext => {
      this.profileForm.patchValue(onNext);
    });
  }

  reset(): void {
    this.profileForm.reset(this.getFormData().subscribe(onNext => {
      this.profileForm.patchValue(onNext);
    }));
  }

  onSubmit({value, valid}: { value: UserProfileDto, valid: boolean }) {
    this.userService.updateUserProfile(value).subscribe(onNext => {
      this.snackBar.open('Your profile was updated successfully', '', {
        duration: 5000,
        verticalPosition: 'top',
        panelClass: 'bg-green'
      });
      this.profileForm.controls['oldPassword'].setValue('');
      this.profileForm.controls['newPassword1'].setValue('');
      this.profileForm.controls['newPassword2'].setValue('');
      this.profileForm.controls['newEmail'].setValue('');
    }, onError => {
      console.log(onError);
      this.snackBar.open('There was a problem updating your profile, please try again later.', '', {
        duration: 5000,
        verticalPosition: 'top',
        panelClass: 'bg-red'
      });
    });
  }

}
