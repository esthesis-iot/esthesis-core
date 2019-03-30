import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../shared/base-component';
import {QFormsService} from '@eurodyn/forms';
import {UUID} from 'angular2-uuid';
import {UserService} from './user.service';
import {UtilityService} from '../shared/utility.service';
import {MatDialog} from '@angular/material';
import {OkCancelModalComponent} from '../shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-user',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  hide1 = true;
  hide2 = true;
  isEdit = false;
  private userTypes: string[];
  private userStatuses: string[];
  // statusControl = new FormControl('', [Validators.required]);
  // typeControl = new FormControl('', [Validators.required]);
  // pattern = '^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-.]).{8,}$';
  // options = {
  //   layers: [
  //     tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18})
  //   ],
  //   zoom: 14,
  //   center: latLng(47.2287109, 14.3009642)
  // };
  // layer1 = [
  //   marker([47.2287109, 14.3009642], {
  //     icon: icon({
  //       iconSize: [25, 41],
  //       iconAnchor: [13, 41],
  //       iconUrl: 'assets/marker-icon.png',
  //       shadowUrl: 'assets/marker-shadow.png'
  //     })
  //   }),
  // ];
  // groupCtrl = new FormControl();
  // filteredGroups: Observable<string[]>;

  // @ViewChild('groupInput') groupInput: ElementRef<HTMLInputElement>;

  constructor(private fb: FormBuilder, private userService: UserService, private route: ActivatedRoute,
              private qForms: QFormsService, private router: Router, private dialog: MatDialog,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    this.loadStatusAndTypes();

    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.isEdit = (this.id !== 0);

    // Setup the form.
    this.form = this.fb.group({
      id: [{value: '', disabled: true}],
      fn: [{value: '', disabled: false}, [Validators.required]],
      ln: [{value: '', disabled: false}, [Validators.maxLength(256)]],
      salt: [{value: '', disabled: true}],
      email: [{
        value: '',
        disabled: this.isEdit
      }, [Validators.maxLength(256), Validators.email, Validators.required]],
      newEmail: [{
        value: '',
        disabled: false
      }, [Validators.maxLength(256), Validators.email, Validators.required]],
      userType: [{value: [], disabled: false}, [Validators.maxLength(1024)]],
      status: [{value: [], disabled: false}, [Validators.maxLength(1024)]],
      createdOn: [{value: '', disabled: true}, [Validators.maxLength(1024)]],
      password: [{
        value: '',
        disabled: true
      }, [Validators.maxLength(1024)]],
      oldPassword: [{
        value: '',
        disabled: true
      }, [Validators.maxLength(1024)]],
      newPassword1: [{
        value: '',
        disabled: false
      }, [Validators.maxLength(1024)]],
      newPassword2: [{
        value: '',
        disabled: false
      }, [Validators.maxLength(1024)]],
    });
    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.userService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    } else {
      this.form.patchValue({
        salt: UUID.UUID()
      });
    }

  }

  private loadStatusAndTypes() {
    this.userService.getUserRoles().subscribe(onNext => {
      this.userTypes = onNext;
    });
    this.userService.getUserStatus().subscribe(onNext => {
      this.userStatuses = onNext;
    });
  }

  save() {
    if (this.id === 0) {
      this.insert();
    } else {
      this.update();
    }
  }

  private update() {
    this.form.patchValue(
      {
        oldPassword: this.form.controls['password'].value
      }
    );
    this.userService.updateUserProfile(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('User was successfully edited.');
      this.router.navigate(['users']);
    });
  }

  private insert() {
    this.form.patchValue(
      {
        password: this.form.controls['newPassword2'].value
      }
    );
    this.userService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('User was successfully created.');
      this.router.navigate(['users']);
    });
  }

  delete() {
      this.dialog.open(OkCancelModalComponent, {
        data: {
          title: 'Delete User',
          question: 'Do you really want to delete this User?',
          buttons: {
            ok: true, cancel: true, reload: false
          }
        }
      }).afterClosed().subscribe(result => {
        if (result) {
          this.userService.delete(this.id).subscribe(onNext => {
            this.utilityService.popupSuccess('User successfully deleted.');
            this.router.navigate(['users']);
          });
        }
      });
  }
}
