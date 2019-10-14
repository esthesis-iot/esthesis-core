import {Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../base-component';
import {ContainersService} from './containers.service';
import {QFormsService} from '@eurodyn/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {VirtualizationDto} from '../../../dto/virtualization-dto';
import {CertificateDto} from '../../../dto/certificate-dto';
import {VirtualizationService} from '../../../infrastructure/infrastructure-virtualization/virtualization.service';
import {CertificatesService} from '../../../certificates/certificates.service';
import {WebSocketService} from 'src/app/services/web-socket.service';

@Component({
  selector: 'app-container-deploy',
  templateUrl: './container-deploy.component.html',
  styleUrls: ['./container-deploy.component.scss']
})
export class ContainerDeployComponent extends BaseComponent implements OnInit, OnDestroy {

  serverForm: FormGroup;
  containerForm: FormGroup;
  virtualizationServers: VirtualizationDto[];
  certificates: CertificateDto[];
  private wsSubscription: Subscription;
  consoleOutput = '';
  showConsole = false;
  @ViewChild('console', { static: true }) private consoleContainer: ElementRef;

  constructor(private formBuilder: FormBuilder,
              private virtualizationService: VirtualizationService,
              private certificatesService: CertificatesService,
              private containersService: ContainersService,
              private qForms: QFormsService, @Inject(MAT_DIALOG_DATA) public data: any,
              private webSocketService: WebSocketService,
              public selfDialogRef: MatDialogRef<ContainerDeployComponent>) {
    super();
  }

  ngOnInit() {
    // Step 1 form.
    this.serverForm = this.formBuilder.group({
      server: ['', Validators.required]
    });

    // Step 2 form.
    this.containerForm = this.formBuilder.group({
      image: ['', Validators.required],
      registryUsername: ['', []],
      registryPassword: ['', []],
      network: ['', []],
      restart: ['NONE', []],
      env: new FormArray([]),
      ports: new FormArray([]),
      volumes: new FormArray([]),
      name: ['', []],
      scale: ['1', []],
    });

    // Get available virtualization infrastructure.
    this.virtualizationService.getAll().subscribe(onNext => {
      this.virtualizationServers = onNext.content;
    });

    // Get certificates.
    this.certificatesService.getAll().subscribe(onNext => {
      this.certificates = onNext.content;
    });

    // Setup WebSocket deployment progress messages.
    this.wsSubscription = this.webSocketService.watch(this.data.wsId).subscribe(onNext => {
      this.consoleOutput += onNext.body + (onNext.body === '.' ? '' : '<br>');
      try {
        this.consoleContainer.nativeElement.scrollTop = this.consoleContainer.nativeElement.scrollHeight;
      } catch (err) {
      }
    });
  }

  addEnv() {
    (this.containerForm.get('env') as FormArray).push(
      this.formBuilder.group({
        envName: ['', Validators.required],
        envValue: ['', Validators.required],
      })
    );
  }

  addVolume() {
    (this.containerForm.get('volumes') as FormArray).push(
      this.formBuilder.group({
        source: ['', Validators.required],
        target: ['', Validators.required],
      })
    );
  }

  addPort() {
    (this.containerForm.get('ports') as FormArray).push(
      this.formBuilder.group({
        host: ['', Validators.required],
        container: ['', Validators.required],
        protocol: ['', Validators.required],
      })
    );
  }

  removeEnv(index: number) {
    (this.containerForm.get('env') as FormArray).removeAt(index);
  }

  removePort(index: number) {
    (this.containerForm.get('ports') as FormArray).removeAt(index);
  }

  removeVolume(index: number) {
    (this.containerForm.get('volumes') as FormArray).removeAt(index);
  }

  pickCertificate(certificateId: number, index: number) {
    this.certificatesService.get(certificateId).subscribe(onNext => {
      (this.containerForm.get('env') as FormArray).controls[index].patchValue(
        {envValue: onNext.certificate});
    });
  }

  pickPrivateKey(certificateId: number, index: number) {
    this.certificatesService.get(certificateId).subscribe(onNext => {
      (this.containerForm.get('env') as FormArray).controls[index].patchValue(
        {envValue: onNext.privateKey});
    });
  }

  deploy() {
    // Prepare the form to submit.
    const form = {
      ...{server: this.serverForm.controls['server'].value['id']},
      ...this.qForms.cleanupForm(this.containerForm),
      ...{wsId: this.data.wsId}
    };

    this.showConsole = true;

    // Submit the form.
    this.containersService.save(form).subscribe(onNext => {
      this.consoleOutput += 'Request submitted successfully.<br>';
    }, onError => {
      try {
        this.consoleOutput += 'ERROR: ' + onError.error.message;
      } catch (e) {
        this.consoleOutput += 'ERROR: Server error.';
      }
    });
    this.consoleOutput += '<br>';
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.webSocketService.unwatch(this.wsSubscription);
    }
  }

  close() {
    this.selfDialogRef.close();
  }

  clearConsole() {
    this.consoleOutput = '';
    this.showConsole = false;
  }
}
