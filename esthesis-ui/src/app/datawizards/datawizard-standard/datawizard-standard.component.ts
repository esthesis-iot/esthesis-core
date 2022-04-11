import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NifiSinkService} from '../../nifisinks/nifi-sink.service';
import {AppConstants} from '../../app.constants';
import 'rxjs-compat/add/operator/concat';
import 'rxjs-compat/add/observable/concat';
import {NiFiSinkDto} from '../../dto/nifisinks/nifi-sink-dto';
import {UtilityService} from '../../shared/service/utility.service';
import {safeDump} from 'js-yaml';
import {Log} from "ng2-logger/browser";

@Component({
  selector: 'app-datawizard-standard',
  templateUrl: './datawizard-standard.component.html',
  styleUrls: ['./datawizard-standard.component.scss']
})
export class DatawizardStandardComponent implements OnInit {
  // Logger.
  private log = Log.create('DatawizardStandardComponent');
  form!: FormGroup;
  wizardProgress = 0;

  constructor(private fb: FormBuilder, private nifiSinfService: NifiSinkService,
              private utilityService: UtilityService) {
  }

  ngOnInit(): void {
    // Setup the form.
    this.form = this.fb.group({
      mqttUri: ['tcp://esthesis-mqtt:1883', [Validators.required]],
      esthesisDbUri: ['jdbc:mysql://esthesis-db:3306/esthesis', [Validators.required]],
      esthesisDbDriver: ['com.mysql.cj.jdbc.Driver', [Validators.required]],
      esthesisDbDriverLocation: ['/opt/nifi/nifi-current/lib/mysql-connector-java-8.0.21.jar', [Validators.required]],
      esthesisDbUser: ['esthesis', [Validators.required]],
      esthesisDbPassword: ['esthesis', [Validators.required]],
      influxDbUri: ['http://esthesis-tsdb:8086', [Validators.required]],
      influxDbUser: ['esthesis', [Validators.required]],
      influxDbPassword: ['esthesis', [Validators.required]],
    });
  }

  // @ts-ignore
  async executeWizard() {
    const requests: NiFiSinkDto[] = [
      {
        configuration: safeDump(Object.entries({
          uri: this.form!.get('mqttUri')!.value,
          topic: 'esthesis/ping/#',
          qos: 0,
          queueSize: 1000,
          schedulingPeriod: '100 ms',
          keystoreFilename: null,
          keystorePassword: null,
          truststoreFilename: null,
          truststorePassword: null
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.readers.mqtt.ConsumeMQTT',
        name: 'Ping reader (from MQTT)',
        handler: AppConstants.HANDLER.PING,
        state: true,
        type: 'readers'
      },
      {
        configuration: safeDump(Object.entries({
          databaseConnectionURL: this.form!.get('esthesisDbUri')!.value,
          databaseDriverClassName: this.form!.get('esthesisDbDriver')!.value,
          databaseDriverClassLocation: this.form!.get('esthesisDbDriverLocation')!.value,
          databaseUser: this.form!.get('esthesisDbUser')!.value,
          password: this.form!.get('esthesisDbPassword')!.value,
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.writers.relational.PutDatabaseRecord',
        name: 'Ping writer (to esthesis database)',
        handler: AppConstants.HANDLER.PING,
        state: true,
        type: 'writers'
      },
      {
        configuration: safeDump(Object.entries({
          uri: this.form!.get('mqttUri')!.value,
          topic: 'esthesis/metadata/#',
          qos: 0,
          queueSize: 1000,
          schedulingPeriod: '100 ms',
          keystoreFilename: null,
          keystorePassword: null,
          truststoreFilename: null,
          truststorePassword: null
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.readers.mqtt.ConsumeMQTT',
        name: 'Metadata reader (from MQTT)',
        handler: AppConstants.HANDLER.METADATA,
        state: true,
        type: 'readers'
      },
      {
        configuration: safeDump(Object.entries({
          uri: this.form!.get('mqttUri')!.value,
          topic: 'esthesis/telemetry/#',
          qos: 0,
          queueSize: 1000,
          schedulingPeriod: '100 ms',
          keystoreFilename: null,
          keystorePassword: null,
          truststoreFilename: null,
          truststorePassword: null
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.readers.mqtt.ConsumeMQTT',
        name: 'Telemetry reader (from MQTT)',
        handler: AppConstants.HANDLER.TELEMETRY,
        state: true,
        type: 'readers'
      },
      {
        configuration: safeDump(Object.entries({
          databaseConnectionURL: this.form!.get('esthesisDbUri')!.value,
          databaseDriverClassName: this.form!.get('esthesisDbDriver')!.value,
          databaseDriverClassLocation: this.form!.get('esthesisDbDriverLocation')!.value,
          databaseUser: this.form!.get('esthesisDbUser')!.value,
          password: this.form!.get('esthesisDbPassword')!.value,
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.writers.relational.PutDatabaseRecord',
        name: 'Metadata writer (to esthesis database)',
        handler: AppConstants.HANDLER.METADATA,
        state: true,
        type: 'writers'
      },
      {
        configuration: safeDump(Object.entries({
          username: this.form!.get('influxDbUser')!.value,
          password: this.form!.get('influxDbPassword')!.value,
          databaseName: 'esthesis',
          databaseUrl: this.form!.get('influxDbUri')!.value,
          retentionPolicy: 'autogen',
          maxConnectionTimeoutSeconds: 60,
          consistencyLevel: 'ANY',
          charset: 'UTF-8',
          maxRecordSize: 100,
          maxRecordSizeUnit: 'kb',
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.writers.influxdb.PutInfluxDB',
        name: 'Telemetry writer (to InfluxDB)',
        handler: AppConstants.HANDLER.TELEMETRY,
        state: true,
        type: 'writers'
      },
      {
        configuration: safeDump(Object.entries({
          databaseConnectionURL: this.form!.get('esthesisDbUri')!.value,
          databaseDriverClassName: this.form!.get('esthesisDbDriver')!.value,
          databaseDriverClassLocation: this.form!.get('esthesisDbDriverLocation')!.value,
          databaseUser: this.form!.get('esthesisDbUser')!.value,
          password: this.form!.get('esthesisDbPassword')!.value,
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.producers.relational.ExecuteSQL',
        name: 'Metadata producer (from esthesis database)',
        handler: AppConstants.HANDLER.METADATA,
        state: true,
        type: 'producers'
      },
      {
        configuration: safeDump(Object.entries({
          username: this.form!.get('influxDbUser')!.value,
          password: this.form!.get('influxDbPassword')!.value,
          databaseName: 'esthesis',
          databaseUrl: this.form!.get('influxDbUri')!.value,
          maxConnectionTimeoutSeconds: 60,
          queryResultTimeUnit: 'Nanoseconds',
          queryChunkSize: 1,
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.producers.influxdb.ExecuteInfluxDB',
        name: 'Telemetry producer (from InfluxDB)',
        handler: AppConstants.HANDLER.TELEMETRY,
        state: true,
        type: 'producers'
      },
      {
        configuration: safeDump(Object.entries({
          databaseConnectionURL: this.form!.get('esthesisDbUri')!.value,
          databaseDriverClassName: this.form!.get('esthesisDbDriver')!.value,
          databaseDriverClassLocation: this.form!.get('esthesisDbDriverLocation')!.value,
          databaseUser: this.form!.get('esthesisDbUser')!.value,
          password: this.form!.get('esthesisDbPassword')!.value,
          uri: this.form!.get('mqttUri')!.value,
          topic: 'esthesis/control/request',
          qos: 0,
          retainMessage: false,
          schedulingPeriod: '100 ms',
          keystoreFilename: null,
          keystorePassword: null,
          truststoreFilename: null,
          truststorePassword: null
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.producers.command.CommandProducer',
        name: 'Command Producer',
        handler: AppConstants.HANDLER.COMMAND,
        state: true,
        type: 'producers'
      },
      {
        configuration: safeDump(Object.entries({
          uri: this.form!.get('mqttUri')!.value,
          topic: 'esthesis/control/reply/#',
          qos: 0,
          queueSize: 1000,
          schedulingPeriod: '100 ms',
          keystoreFilename: null,
          keystorePassword: null,
          truststoreFilename: null,
          truststorePassword: null
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.readers.mqtt.ConsumeMQTT',
        name: 'Command Reply Reader (from MQTT)',
        handler: AppConstants.HANDLER.COMMAND,
        state: true,
        type: 'readers'
      },
      {
        configuration: safeDump(Object.entries({
          databaseConnectionURL: this.form!.get('esthesisDbUri')!.value,
          databaseDriverClassName: this.form!.get('esthesisDbDriver')!.value,
          databaseDriverClassLocation: this.form!.get('esthesisDbDriverLocation')!.value,
          databaseUser: this.form!.get('esthesisDbUser')!.value,
          password: this.form!.get('esthesisDbPassword')!.value,
          schedulingPeriod: '100 ms'
        }).reduce((a: any, [k, v]) => v ? (a[k] = v, a) : a, {})),
        factoryClass: 'esthesis.platform.backend.server.nifi.sinks.writers.relational.PutDatabaseRecord',
        name: 'Command writer (to esthesis database)',
        handler: AppConstants.HANDLER.COMMAND,
        state: true,
        type: 'writers'
      }
    ];

    let index = 0;
    let isError = false;
    let lastRequestName;
    for (const request of requests) {
      try {
        index++;
        lastRequestName = request.name;
        this.log.info("Executing request for: " + lastRequestName);
        this.wizardProgress = Math.floor((index / requests.length) * 100);
        await this.nifiSinfService.save(request).toPromise();
      } catch (e) {
        isError = true;
        this.utilityService.popupError("Could not execute wizard successfully. Please delete" +
          " manually the esthesis NiFi workflow and try again.<br><br>Failed request:<br>" + request.name);
        break;
      }
    }
    this.wizardProgress = 0;

    if (!isError) {
      this.utilityService.popupSuccess('Data wizard terminated successfully.');
    }
  }
}
