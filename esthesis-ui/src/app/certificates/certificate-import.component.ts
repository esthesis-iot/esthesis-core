import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {HttpResponse} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {UtilityService} from '../shared/service/utility.service';
import {CertificatesService} from './certificates.service';

@Component({
  selector: 'app-certificate-import',
  templateUrl: './certificate-import.component.html',
  styleUrls: []
})
export class CertificateImportComponent implements OnInit {

  form!: FormGroup;

  constructor(private fb: FormBuilder, private certificatesService: CertificatesService,
              private route: ActivatedRoute, private router: Router,
              private dialog: MatDialog,
              private utilityService: UtilityService) {
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      backup: ['', [Validators.required]],
    });
  }

  selectFile(event: any) {
    this.form.controls['backup'].patchValue(event.target.files[0]);
  }

  restore() {
    this.certificatesService.restore(this.form).subscribe(success => {
      if (success instanceof HttpResponse) {
        if (success.status === 200) {
          this.utilityService.popupSuccess('Certificate restored successfully.');
          this.router.navigate(['certificates']);
        } else {
          this.utilityService.popupError('Something went wrong, please try again.');
        }
      }
    }, error => {this.utilityService.popupError(error.error);});
  }
}
