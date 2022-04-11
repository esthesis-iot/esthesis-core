import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Log} from 'ng2-logger/browser';
import {FormBuilder, FormGroup} from '@angular/forms';
import {NewPasswordUserDto} from '../dto/new-password-user-dto';
import {UserService} from '../users/user.service';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-new-password',
  templateUrl: './new-password.component.html',
  styleUrls: []
})
export class NewPasswordComponent extends BaseComponent implements OnInit {
  // Logger.
  private log = Log.create('NewPasswordComponent');

  // Form's error message indicator.
  errorMessage: string | undefined;

  // Show/hidePassword password
  hide1 = true;
  hide2 = true;

  // Form control.
  newPasswordForm!: FormGroup;

  constructor(public userService: UserService, private router: Router, private fb: FormBuilder) {
    super();
  }

  ngOnInit(): void {
    this.errorMessage = undefined;
    this.newPasswordForm = this.fb.group({
      username: [''],
      existingPassword: [''],
      newPassword: [''],
    });
  }

  onSubmit({value, valid}: { value: NewPasswordUserDto, valid: boolean }) {
    this.errorMessage = undefined;
  }
}
