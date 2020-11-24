import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NifiSinkService} from '../../nifisinks/nifi-sink.service';
import {AppConstants} from '../../app.constants';
import 'rxjs-compat/add/operator/concat';
import 'rxjs-compat/add/observable/concat';
import {NiFiSinkDto} from '../../dto/nifisinks/nifi-sink-dto';
import {UtilityService} from '../../shared/service/utility.service';
import {safeDump} from 'js-yaml';

@Component({
  selector: 'app-datawizard-standard',
  templateUrl: './datawizard-standard.component.html',
  styleUrls: ['./datawizard-standard.component.scss']
})
export class DatawizardStandardComponent implements OnInit {
  form: FormGroup;
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
      esthesisDbUser: ['root', [Validators.required]],
      esthesisDbPassword: ['root', [Validators.required]],
      influxDbUri: ['http://esthesis-influxdb:8086', [Validators.required]],
      influxDbUser: ['esthesis', [Validators.required]],
      influxDbPassword: ['esthesis', [Validators.required]],
    });
  }

  async executeWizard() {
    const requests: NiFiSinkDto[] = [
      // {
      //   configuration: safeDump(Object.entries({
      //     uri: this.form.get('mqttUri').value,
      //     topic: 'esthesis/ping/#',
      //     qos: 0,
      //     queueSize: 1000,
      //     schedulingPeriod: '100 ms',
      //     keystoreFilename: null,
      //     keystorePassword: null,
      //     truststoreFilename: null,
      //     truststorePassword: null
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.readers.mqtt.ConsumeMQTT',
      //   name: 'Ping reader (from MQTT)',
      //   handler: AppConstants.HANDLER.PING,
      //   state: true,
      //   type: 'readers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     databaseConnectionURL: this.form.get('esthesisDbUri').value,
      //     databaseDriverClassName: this.form.get('esthesisDbDriver').value,
      //     databaseDriverClassLocation: this.form.get('esthesisDbDriverLocation').value,
      //     databaseUser: this.form.get('esthesisDbUser').value,
      //     password: this.form.get('esthesisDbPassword').value,
      //     schedulingPeriod: '100 ms'
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.writers.relational.PutDatabaseRecord',
      //   name: 'Ping writer (to esthesis database)',
      //   handler: AppConstants.HANDLER.PING,
      //   state: true,
      //   type: 'writers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     uri: this.form.get('mqttUri').value,
      //     topic: 'esthesis/metadata/#',
      //     qos: 0,
      //     queueSize: 1000,
      //     schedulingPeriod: '100 ms',
      //     keystoreFilename: null,
      //     keystorePassword: null,
      //     truststoreFilename: null,
      //     truststorePassword: null
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.readers.mqtt.ConsumeMQTT',
      //   name: 'Metadata reader (from MQTT)',
      //   handler: AppConstants.HANDLER.PING,
      //   state: true,
      //   type: 'readers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     uri: this.form.get('mqttUri').value,
      //     topic: 'esthesis/telemetry/#',
      //     qos: 0,
      //     queueSize: 1000,
      //     schedulingPeriod: '100 ms',
      //     keystoreFilename: null,
      //     keystorePassword: null,
      //     truststoreFilename: null,
      //     truststorePassword: null
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.readers.mqtt.ConsumeMQTT',
      //   name: 'Telemetry reader (from MQTT)',
      //   handler: AppConstants.HANDLER.PING,
      //   state: true,
      //   type: 'readers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     databaseConnectionURL: this.form.get('esthesisDbUri').value,
      //     databaseDriverClassName: this.form.get('esthesisDbDriver').value,
      //     databaseDriverClassLocation: this.form.get('esthesisDbDriverLocation').value,
      //     databaseUser: this.form.get('esthesisDbUser').value,
      //     password: this.form.get('esthesisDbPassword').value,
      //     schedulingPeriod: '100 ms'
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.writers.relational.PutDatabaseRecord',
      //   name: 'Metadata writer (to esthesis database)',
      //   handler: AppConstants.HANDLER.METADATA,
      //   state: true,
      //   type: 'writers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     username: this.form.get('influxDbUser').value,
      //     password: this.form.get('influxDbPassword').value,
      //     databaseName: 'esthesis',
      //     databaseUrl: this.form.get('influxDbUri').value,
      //     retentionPolicy: 'autogen',
      //     maxConnectionTimeoutSeconds: 60,
      //     consistencyLevel: 'ANY',
      //     charset : 'UTF-8',
      //     maxRecordSize: 100,
      //     maxRecordSizeUnit: 'kb',
      //     schedulingPeriod: '100 ms'
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.writers.influxdb.PutInfluxDB',
      //   name: 'Telemetry writer (to InfluxDB)',
      //   handler: AppConstants.HANDLER.TELEMETRY,
      //   state: true,
      //   type: 'writers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     databaseConnectionURL: this.form.get('esthesisDbUri').value,
      //     databaseDriverClassName: this.form.get('esthesisDbDriver').value,
      //     databaseDriverClassLocation: this.form.get('esthesisDbDriverLocation').value,
      //     databaseUser: this.form.get('esthesisDbUser').value,
      //     password: this.form.get('esthesisDbPassword').value,
      //     schedulingPeriod: '100 ms'
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.producers.relational.ExecuteSQL',
      //   name: 'Metadata producer (from esthesis database)',
      //   handler: AppConstants.HANDLER.METADATA,
      //   state: true,
      //   type: 'producers'
      // },
      // {
      //   configuration: safeDump(Object.entries({
      //     username: this.form.get('influxDbUser').value,
      //     password: this.form.get('influxDbPassword').value,
      //     databaseName: 'esthesis',
      //     databaseUrl: this.form.get('influxDbUri').value,
      //     maxConnectionTimeoutSeconds: 60,
      //     queryResultTimeUnit: 'Nanoseconds',
      //     queryChunkSize: 1,
      //     schedulingPeriod: '100 ms'
      //   }).reduce((a,[k,v]) => (v ? (a[k]=v, a) : a), {})),
      //   factoryClass: 'esthesis.backend.nifi.sinks.producers.influxdb.ExecuteInfluxDB',
      //   name: 'Telemetry producer (from InfluxDB)',
      //   handler: AppConstants.HANDLER.TELEMETRY,
      //   state: true,
      //   type: 'producers'
      // },
    ];

    let index = 0;
    for (const request of requests) {
      try {
        index++;
        this.wizardProgress = Math.floor((index / requests.length) * 100);
        await this.nifiSinfService.save(request).toPromise();
      } catch (e) {
        this.utilityService.popupError('Could not execute wizard successfully. Please delete' +
          ' manually the esthesis NiFi workflow and try again.');
        break;
      }
    }
    this.wizardProgress = 0;
  }
}
