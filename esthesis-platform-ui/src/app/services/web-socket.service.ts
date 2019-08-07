import {Injectable} from '@angular/core';
import {Log} from 'ng2-logger/browser';
import {InjectableRxStompConfig, RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {Observable, Subscription} from 'rxjs';
import {AppConstants} from '../app.constants';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  // Logger.
  private logger = Log.create('WebSocketService');

  constructor(private rxStompService: RxStompService) {
  }

  connect() {
    // Check if a secure or plain WebSocket connection should be established.
    var wsProtocol = "ws:";
    if (window.location.protocol === 'https:') {
      wsProtocol = "wss:";
    }

    // Connect to WebSocket.
    const rxStompConfig: InjectableRxStompConfig = {
      // STOMP server.
      brokerURL: `${wsProtocol}//${window.location.hostname}:${window.location.port}/api/ws?bearer=${localStorage.getItem(AppConstants.JWT_STORAGE_NAME)}`,

      // How often to heartbeat?
      // Interval in milliseconds, set to 0 to disable
      heartbeatIncoming: 0, // Typical value 0 - disabled
      heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

      // Wait in milliseconds before attempting auto reconnect
      // Set to 0 to disable
      reconnectDelay: 1000,

      // Will log diagnostics on console
      // It can be quite verbose, not recommended in production
      // Skip this key to stop logging to console
      // debug: (msg: string): void => {
      //   console.log(new Date(), msg);
      // }
    };

    this.logger.info('Connecting WebSocket client.');
    this.rxStompService.configure(rxStompConfig);
    this.rxStompService.activate();
  }

  disconnect() {
    this.logger.info('Disconnecting WebSocket client.');
    this.rxStompService.deactivate();
  }

  watch(topicName: string): Observable<Message> {
    if (!topicName.startsWith('/')) {
      topicName = '/' + topicName;
    }
    topicName = AppConstants.WEBSOCKET.TOPIC_PREFIX + topicName;
    this.logger.info('Watching topic: ' + topicName);
    return this.rxStompService.watch(topicName);
  }

  unwatch(subscription: Subscription) {
    this.logger.info('Unwatching topic: ', subscription);
    subscription.unsubscribe();
    subscription = null;
  }
}
