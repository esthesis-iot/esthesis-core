import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../shared/component/base-component';
import {Router} from '@angular/router';
import {NiFiService} from '../infrastructure/infrastructure-nifi/nifi.service';

@Component({
  selector: 'app-datawizards',
  templateUrl: './datawizards.component.html',
  styleUrls: ['./datawizards.component.scss']
})
export class DatawizardsComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  WIZARD_STANDARD = 'standard';
  activeNiFiId: any | undefined;

  constructor(private fb: FormBuilder, private router: Router, private nifiService: NiFiService) {
    super();
  }

  ngOnInit(): void {
    // Setup the form.
    this.form = this.fb.group({
      wizard: [this.WIZARD_STANDARD, [Validators.required]]
    });

    this.nifiService.getActive().subscribe(value => {
      this.activeNiFiId = value?.id;
    });
  }

  executeWizard() {
    this.router.navigate(['data-wizards', this.form!.get('wizard')!.value]);
  }
}
