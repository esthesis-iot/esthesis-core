import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {BaseComponent} from "../shared/component/base-component";
import {Router} from "@angular/router";

@Component({
  selector: "app-data-wizards",
  templateUrl: "./datawizards.component.html",
  styleUrls: ["./datawizards.component.scss"]
})
export class DatawizardsComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  WIZARD_STANDARD = "standard";
  activeNiFiId: any | undefined;

  constructor(private fb: FormBuilder, private router: Router) {
    super();
  }

  ngOnInit(): void {
    // Set up the form.
    this.form = this.fb.group({
      wizard: [this.WIZARD_STANDARD, [Validators.required]]
    });

  }

  executeWizard() {
    this.router.navigate(["data-wizards", this.form.get("wizard")!.value]);
  }
}
