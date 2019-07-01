import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material';
import {CommandComponent} from './command.component';

@Injectable({
  providedIn: 'root'
})
export class CommandPopupService {
  constructor(private dialog: MatDialog) {
  }

  commandPopup() {
    const dialogRef = this.dialog.open(CommandComponent, {
      height: '70%',
      width: '80%',
      data: {
        // wsId: wsId
      }
    });
  }
}
