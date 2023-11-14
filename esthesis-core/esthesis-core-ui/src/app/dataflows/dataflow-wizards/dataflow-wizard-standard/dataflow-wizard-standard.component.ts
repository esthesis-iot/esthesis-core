import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CertificatesService} from "../../../certificates/certificates.service";
import {QFormsService} from "@qlack/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {AppConstants} from "../../../app.constants";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {DataflowsService} from "../../dataflows.service";
import {WizardDataflowDto} from "../../dto/wizard-dataflow";
import {forkJoin} from "rxjs";

@Component({
  selector: "app-dataflow-wizard-standard",
  templateUrl: "./dataflow-wizard-standard.component.html"
})
export class DataflowWizardStandardComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  namespaces: string[] = [];

  constructor(private fb: FormBuilder, private certificatesService: CertificatesService,
    private qForms: QFormsService,
    private route: ActivatedRoute, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    private dataflowService: DataflowsService) {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      wizardName: ["standard", [Validators.required]],
      version: ["3.0.2", [Validators.required]],
      kafkaClusterUrl: ["kafka-headless:9094", [Validators.required]],
      mongoDbClusterUrl: ["mongodb://mongodb:27017", [Validators.required]],
      mongoDbDatabase: ["esthesiscore", [Validators.required]],
      mongoDbUsername: ["esthesis-system", [Validators.required]],
      mongoDbPassword: ["esthesis-system", [Validators.required]],
      namespace: ["default", [Validators.required]],
      influxDbClusterUrl: ["http://influxdb:8086", [Validators.required]],
      influxDbOrg: ["esthesis", [Validators.required]],
      influxDbBucket: ["esthesis", [Validators.required]],
      influxDbToken: ["", [Validators.required]],
      redisClusterUrl: ["redis://:esthesis-system@redis-headless:6379/0", [Validators.required]],
      dockerRegistry: ["", []],
    });

    this.dataflowService.getNamespaces().subscribe({
      next: onNext => {
        this.namespaces = onNext;
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Could not get available namespaces", err);
      }
    });
  }

  execute() {
    // Ping DFL.
    const pingDfl: WizardDataflowDto = {
      type: "ping-updater",
      name: "dfl-ping-updater",
      description: "Ping updater, created by wizard",
      status: true,
      config: {
        kafka: {
          "cluster-url": this.form.get("kafkaClusterUrl")?.value,
          "consumer-group": "dfl-ping-updater",
          "ping-topic": "esthesis-ping",
        },
        "esthesis-db-url": this.form.get("mongoDbClusterUrl")?.value,
        "esthesis-db-name": this.form.get("mongoDbDatabase")?.value,
        "esthesis-db-username": this.form.get("mongoDbUsername")?.value,
        "esthesis-db-password": this.form.get("mongoDbPassword")?.value,
        "queue-size": "100",
        "poll-timeout": "1000",
        consumers: "10",
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      },
      kubernetes: {
        namespace: this.form.get("namespace")?.value,
        docker: this.form.get("version")?.value,
        "cpu-request": "250m",
        "cpu-limit": "1",
        "pods-min": "1",
        "pods-max": "10",
      }
    };
    if (this.form.get("dockerRegistry")?.value) {
        pingDfl.image = {
          registry: this.form.get("dockerRegistry")?.value
        };
    }
    const pingDflServiceCall = this.dataflowService.save(pingDfl);

    // Command Reply Updater DFL.
    const commandReplyUpdatedDfl: WizardDataflowDto = {
      type: "command-reply-updater",
      name: "dfl-command-reply-updater",
      description: "Command Reply updater, created by wizard",
      status: true,
      config: {
        kafka: {
          "cluster-url": this.form.get("kafkaClusterUrl")?.value,
          "consumer-group": "dfl-command-reply-updater",
          "command-reply-topic": "esthesis-command-reply",
        },
        "esthesis-db-url": this.form.get("mongoDbClusterUrl")?.value,
        "esthesis-db-name": this.form.get("mongoDbDatabase")?.value,
        "esthesis-db-username": this.form.get("mongoDbUsername")?.value,
        "esthesis-db-password": this.form.get("mongoDbPassword")?.value,
        "queue-size": "100",
        "poll-timeout": "1000",
        consumers: "10",
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      },
      kubernetes: {
        namespace: this.form.get("namespace")?.value,
        docker: this.form.get("version")?.value,
        "cpu-request": "250m",
        "cpu-limit": "1",
        "pods-min": "1",
        "pods-max": "10",
      }
    };
    if (this.form.get("dockerRegistry")?.value) {
      commandReplyUpdatedDfl.image = {
        registry: this.form.get("dockerRegistry")?.value
      };
    }
    const commandReplyUpdatedServiceCall = this.dataflowService.save(commandReplyUpdatedDfl);

    // MQTT Client DFL.
    const mqttClientDfl: WizardDataflowDto = {
      type: "mqtt-client",
      name: "dfl-mqtt-client",
      description: "MQTT client, created by wizard",
      status: true,
      config: {
        "mqtt-broker": {
          "cluster-url": "tcp://mosquitto:1883",
        },
        "mqtt-topic": {
          ping: "esthesis/ping",
          telemetry: "esthesis/telemetry",
          metadata: "esthesis/metadata",
          "command-request": "esthesis/command/request",
          "command-reply": "esthesis/command/reply",
        },
        kafka: {
          "cluster-url": this.form.get("kafkaClusterUrl")?.value,
        },
        "kafka-topic": {
          ping: "esthesis-ping",
          telemetry: "esthesis-telemetry",
          metadata: "esthesis-metadata",
          "command-request": "esthesis-command-request",
          "command-reply": "esthesis-command-reply",
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      },
      kubernetes: {
        namespace: this.form.get("namespace")?.value,
        docker: this.form.get("version")?.value,
        "cpu-request": "250m",
        "cpu-limit": "1",
        "pods-min": "1",
        "pods-max": "10",
      }
    };
    if (this.form.get("dockerRegistry")?.value) {
      mqttClientDfl.image = {
        registry: this.form.get("dockerRegistry")?.value
      };
    }
    const mqttClientServiceCall = this.dataflowService.save(mqttClientDfl);

    // InfluxDB Writer DFL.
    const influxDbWriterDfl: WizardDataflowDto = {
      type: "influxdb-writer",
      name: "dfl-influxdb-writer",
      description: "InfluxDB writer, created by wizard",
      status: true,
      config: {
        "influx-url": this.form.get("influxDbClusterUrl")?.value,
        "influx-org": this.form.get("influxDbOrg")?.value,
        "influx-bucket": this.form.get("influxDbBucket")?.value,
        "influx-token": this.form.get("influxDbToken")?.value,
        kafka: {
          "cluster-url": this.form.get("kafkaClusterUrl")?.value,
          "consumer-group": "dfl-influxdb-writer",
          "telemetry-topic": "esthesis-telemetry",
          "metadata-topic": "esthesis-metadata",
        },
        "esthesis-db-url": this.form.get("mongoDbClusterUrl")?.value,
        "esthesis-db-name": this.form.get("mongoDbDatabase")?.value,
        "esthesis-db-username": this.form.get("mongoDbUsername")?.value,
        "esthesis-db-password": this.form.get("mongoDbPassword")?.value,
        "queue-size": "100",
        "poll-timeout": "1000",
        consumers: "10",
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      },
      kubernetes: {
        namespace: this.form.get("namespace")?.value,
        docker: this.form.get("version")?.value,
        "cpu-request": "250m",
        "cpu-limit": "1",
        "pods-min": "1",
        "pods-max": "10",
      }
    };
    if (this.form.get("dockerRegistry")?.value) {
      influxDbWriterDfl.image = {
        registry: this.form.get("dockerRegistry")?.value
      };
    }
    const influxDbWriterServiceCall = this.dataflowService.save(influxDbWriterDfl);

    // Redis Cache DFL.
    const redisCacheDfl: WizardDataflowDto = {
      type: "redis-cache",
      name: "dfl-redis-cache",
      description: "Redis cache, created by wizard",
      status: true,
      config: {
        redis: {
          url: this.form.get("redisClusterUrl")?.value,
          "max-size": "1024",
          ttl: "0"
        },
        kafka: {
          "cluster-url": this.form.get("kafkaClusterUrl")?.value,
          "consumer-group": "dfl-redis-cache",
          "telemetry-topic": "esthesis-telemetry",
          "metadata-topic": "esthesis-metadata",
        },
        "queue-size": "100",
        "poll-timeout": "1000",
        consumers: "10",
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      },
      kubernetes: {
        namespace: this.form.get("namespace")?.value,
        docker: this.form.get("version")?.value,
        "cpu-request": "250m",
        "cpu-limit": "1",
        "pods-min": "1",
        "pods-max": "10",
      }
    };
    if (this.form.get("dockerRegistry")?.value) {
      redisCacheDfl.image = {
        registry: this.form.get("dockerRegistry")?.value
      };
    }
    const redisCacheServiceCall = this.dataflowService.save(redisCacheDfl);

    if (!this.form.valid) {
      this.utilityService.popupError("You need to fill in all required fields");
    } else {
      // Execute all service calls with a forkJoin.
      forkJoin({ping: pingDflServiceCall, commandReply: commandReplyUpdatedServiceCall, mqtt: mqttClientServiceCall,
        influx: influxDbWriterServiceCall, redis: redisCacheServiceCall}).subscribe({
        next: () => {
          this.utilityService.popupSuccess("Wizard was executed successfully");
          this.router.navigate(["/dataflow"]);
        }, error: () => {
          this.utilityService.popupError("Failed to execute the wizard, check which dataflows have already been created");
        }
      });
    }
  }
}
