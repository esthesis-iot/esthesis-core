import {Component, OnInit, Renderer2} from '@angular/core';
import {Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Log} from 'ng2-logger/browser';
import {BaseComponent} from '../shared/base-component';
import {AppConstants} from '../app.constants';
import {LoginInfoDto} from '../dto/login-info-dto';
import {UserService} from '../users/user.service';
import {WebSocketService} from '../services/web-socket.service';
import {UtilityService} from '../shared/utility.service';
import {BodyBackgroundService} from '../services/body-background.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent extends BaseComponent implements OnInit {
  // Logger.
  private log = Log.create('LoginComponent');

  // Form control.
  loginForm: FormGroup;

  // Show/hidePassword password
  hidePassword = true;

  // Show/Hide login form.
  hideLoginForm = false;

  // Error message to display in form.
  errorMessage: string;

  constructor(private router: Router, private userService: UserService, private fb: FormBuilder,
              private wsService: WebSocketService, private utilityService: UtilityService,
              private renderer: Renderer2, private bodyBackgroundService: BodyBackgroundService) {
    super();
    bodyBackgroundService.getImageUrl().subscribe(onNext => {
      this.renderer.setAttribute(document.body, 'style', 'background-image:  linear-gradient(to top, rgba(0,0,0,0)' +
        ' 30%, rgba(255,255,255,0.62) 64%, rgba(255,255,255,1) 89%), url(\'' + onNext + '\'); background-size: cover;');
    });
  }

  ngOnInit() {
    // If the user is already logged in just redirect back to the originating application.
    if (this.isLoggedIn()) {
      this.log.data('User is already logged in.');
      this.wsService.connect();
      this.router.navigate(['dashboard']);
    }

    // Prepare login form.
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit({value}: { value: LoginInfoDto }) {
    this.userService.login(value).subscribe(
      onNext => {
        this.hideLoginForm = true;
        this.renderer.removeAttribute(document.body, 'style');
        // Save the JWT to be used in future requests.
        // sessionStorage.setItem(AppConstants.JWT_STORAGE_NAME, onNext["jwt"]);
        localStorage.setItem(AppConstants.JWT_STORAGE_NAME, onNext['jwt']);
        this.wsService.connect();
        this.router.navigate(['dashboard']);
      }, onError => {
        console.log(onError);
        this.utilityService.popupError('Authentication was unsuccessful.');
        // this.ngProgressService.complete();
        this.hideLoginForm = false;
      });
  }

}
