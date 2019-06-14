import {Component, Renderer2} from '@angular/core';
import {Log} from 'ng2-logger/browser';
import {UserService} from '../users/user.service';
import {WebSocketService} from '../services/web-socket.service';
import {BodyBackgroundService} from '../services/body-background.service';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent extends BaseComponent {
  private log = Log.create('LogoutComponent');

  constructor(private userService: UserService, private wsService: WebSocketService,
              private renderer: Renderer2, private bodyBackgroundService: BodyBackgroundService) {
    super();

    bodyBackgroundService.getImageUrl().subscribe(onNext => {
      this.renderer.setAttribute(document.body, 'style',
        'background-image:  linear-gradient(to top, rgba(0,0,0,0)' +
        ' 30%, rgba(255,255,255,0.62) 64%, rgba(255,255,255,1) 89%), url(\'' + onNext + '\');' +
        ' background-size: cover;');
    });

    this.userService.logout().subscribe(onNext => {
      this.wsService.disconnect();
      this.log.data('Successfully terminated session.');
      localStorage.removeItem(this.constants.JWT_STORAGE_NAME);
    }, onError => {
      this.log.error('Could not terminate session.');
      localStorage.removeItem(this.constants.JWT_STORAGE_NAME);
    });
  }
}
