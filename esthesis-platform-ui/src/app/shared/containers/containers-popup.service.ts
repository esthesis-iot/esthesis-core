import {Injectable} from '@angular/core';
import {MatDialog} from '@angular/material';
import {ContainerDeployComponent} from './container-deploy.component';
import {UUID} from 'angular2-uuid';

@Injectable({
  providedIn: 'root'
})
export class ContainersPopupService {
  constructor(private dialog: MatDialog) {
  }

  deployContainerPopup() {
    // Create a random topic to monitor this deployment via WebSockets.
    const wsId = UUID.UUID();
    const dialogRef = this.dialog.open(ContainerDeployComponent, {
      height: '70%',
      width: '80%',
      data: {
        wsId: wsId
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log(result);
      }
    });
  }
}
