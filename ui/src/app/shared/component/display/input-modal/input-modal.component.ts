import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: "app-input-modal",
  templateUrl: "./input-modal.component.html",
  styleUrls: []
})
export class InputModalComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<InputModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  ngOnInit() {
  }

  public reload(): void {
    window.location.reload();
  }

}
