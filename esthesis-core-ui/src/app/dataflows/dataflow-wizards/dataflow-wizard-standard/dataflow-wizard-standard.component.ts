import {Component, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {Router} from "@angular/router";
import {DataflowsService} from "../../dataflows.service";
import {UtilityService} from "../../../shared/services/utility.service";
import {AppConstants} from "../../../app.constants";
import {FormGroup} from "@angular/forms";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {concatMap, delay, from, Observable, tap} from "rxjs";
import * as _ from "lodash-es";
import {DATAFLOW_WIZARD_STANDARD} from "./dto/dataflow-wizard-standard-dto";

@Component({
  selector: "app-dataflow-wizard-standard",
  templateUrl: "./dataflow-wizard-standard.component.html"
})
export class DataflowWizardStandardComponent extends SecurityBaseComponent implements OnInit {
  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [];
  model = {};

  constructor(private readonly dataflowService: DataflowsService,
    private readonly utilityService: UtilityService,
    private readonly router: Router) {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW);
  }

  ngOnInit(): void {
    this.fields = DATAFLOW_WIZARD_STANDARD.fields;
  }

  private createPingUpdaterCall(): {} {
    return {
      type: "ping-updater",
      name: "dfl-ping-updater",
      description: "Ping updater, created by standard wizard.",
      status: true,
      version: _.get(this.model, "kubernetes.container-image-version"),
      config: {
        "esthesis-db": {
          name: _.get(this.model, "config.esthesis-db.name"),
          url: _.get(this.model, "config.esthesis-db.url"),
          username: _.get(this.model, "config.esthesis-db.username"),
          password: _.get(this.model, "config.esthesis-db.password")
        },
        influx: {
          bucket: _.get(this.model, "config.influx.bucket"),
          org: _.get(this.model, "config.influx.org"),
          url: _.get(this.model, "config.influx.url"),
          token: _.get(this.model, "config.influx.token")
        },
        kafka: {
          "cluster-url": _.get(this.model, "config.kafka.cluster-url"),
          "jaas-config": _.get(this.model, "config.kafka.jaas-config"),
          "sasl-mechanism": _.get(this.model, "config.kafka.sasl-mechanism"),
          "security-protocol": _.get(this.model, "config.kafka.security-protocol"),
          "consumer-group": "dfl-ping-updater",
          "ping-topic": "esthesis-ping",
        },
        kubernetes: {
          "cpu-limit": _.get(this.model, "config.kubernetes.cpu-limit"),
          "cpu-request": _.get(this.model, "config.kubernetes.cpu-request"),
          "pods-max": _.get(this.model, "config.kubernetes.pods-max"),
          "pods-min": _.get(this.model, "config.kubernetes.pods-min"),
          namespace: _.get(this.model, "config.kubernetes.namespace"),
          registry: _.get(this.model, "config.kubernetes.registry"),
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        },
        redis: {
          "max-size": _.get(this.model, "config.redis.max-size"),
          ttl: _.get(this.model, "config.redis.ttl"),
          url: _.get(this.model, "config.redis.url"),
        },
        concurrency: {
          consumers: 10,
          "poll-timeout": 1000,
          "queue-size": 1000
        },
      }
    };
  }

  private createCommandReplyUpdater(): {} {
    return {
      type: "command-reply-updater",
      name: "dfl-command-reply-updater",
      description: "Command Reply updater, created by standard wizard.",
      status: true,
      version: _.get(this.model, "kubernetes.container-image-version"),
      config: {
        concurrency: {
          consumers: 10,
          "poll-timeout": 1000,
          "queue-size": 1000
        },
        "esthesis-db": {
          name: _.get(this.model, "config.esthesis-db.name"),
          url: _.get(this.model, "config.esthesis-db.url"),
          username: _.get(this.model, "config.esthesis-db.username"),
          password: _.get(this.model, "config.esthesis-db.password")
        },
        kafka: {
          "cluster-url": _.get(this.model, "config.kafka.cluster-url"),
          "jaas-config": _.get(this.model, "config.kafka.jaas-config"),
          "sasl-mechanism": _.get(this.model, "config.kafka.sasl-mechanism"),
          "security-protocol": _.get(this.model, "config.kafka.security-protocol"),
          "consumer-group": "dfl-command-reply-updater",
          "command-reply-topic": "esthesis-command-reply",
        },
        kubernetes: {
          "cpu-limit": _.get(this.model, "config.kubernetes.cpu-limit"),
          "cpu-request": _.get(this.model, "config.kubernetes.cpu-request"),
          "pods-max": _.get(this.model, "config.kubernetes.pods-max"),
          "pods-min": _.get(this.model, "config.kubernetes.pods-min"),
          namespace: _.get(this.model, "config.kubernetes.namespace"),
          registry: _.get(this.model, "config.kubernetes.registry"),
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      }
    };
  }

  private createMqttClient(): {} {
    return {
      type: "mqtt-client",
      name: "dfl-mqtt-client",
      description: "MQTT client, created by standard wizard.",
      status: true,
      version: _.get(this.model, "kubernetes.container-image-version"),
      config: {
        concurrency: {
          consumers: 10,
          "poll-timeout": 1000,
          "queue-size": 1000
        },
        "mqtt-broker": {
          "cluster-url": _.get(this.model, "config.mqtt-broker.cluster-url"),
          "keep-alive-interval": 30
        },
        mqtt: {
          "ping-topic": "esthesis/ping",
          "telemetry-topic": "esthesis/telemetry",
          "metadata-topic": "esthesis/metadata",
          "command-request-topic": "esthesis/command/request",
          "command-reply-topic": "esthesis/command/reply"
        },
        kafka: {
          "cluster-url": _.get(this.model, "config.kafka.cluster-url"),
          "jaas-config": _.get(this.model, "config.kafka.jaas-config"),
          "sasl-mechanism": _.get(this.model, "config.kafka.sasl-mechanism"),
          "security-protocol": _.get(this.model, "config.kafka.security-protocol"),
          "consumer-group": "dfl-mqtt-client",
          "ping-topic": "esthesis-ping",
          "telemetry-topic": "esthesis-telemetry",
          "metadata-topic": "esthesis-metadata",
          "command-request-topic": "esthesis-command-request",
          "command-reply-topic": "esthesis-command-reply"
        },
        kubernetes: {
          "cpu-limit": _.get(this.model, "config.kubernetes.cpu-limit"),
          "cpu-request": _.get(this.model, "config.kubernetes.cpu-request"),
          "pods-max": _.get(this.model, "config.kubernetes.pods-max"),
          "pods-min": _.get(this.model, "config.kubernetes.pods-min"),
          namespace: _.get(this.model, "config.kubernetes.namespace"),
          registry: _.get(this.model, "config.kubernetes.registry"),
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      }
    };
  }

  private createInfluxDBWriter(): {} {
    return {
      type: "influxdb-writer",
      name: "dfl-influxdb-writer",
      description: "InfluxDB writer, created by standard wizard.",
      status: true,
      version: _.get(this.model, "kubernetes.container-image-version"),
      config: {
        influx: {
          bucket: _.get(this.model, "config.influx.bucket"),
          org: _.get(this.model, "config.influx.org"),
          url: _.get(this.model, "config.influx.url"),
          token: _.get(this.model, "config.influx.token")
        },
        kafka: {
          "consumer-group": "dfl-influxdb-writer",
          "telemetry-topic": "esthesis-telemetry",
          "metadata-topic": "esthesis-metadata",
          "cluster-url": _.get(this.model, "config.kafka.cluster-url"),
          "jaas-config": _.get(this.model, "config.kafka.jaas-config"),
          "sasl-mechanism": _.get(this.model, "config.kafka.sasl-mechanism"),
          "security-protocol": _.get(this.model, "config.kafka.security-protocol"),
        },
        kubernetes: {
          "cpu-limit": _.get(this.model, "config.kubernetes.cpu-limit"),
          "cpu-request": _.get(this.model, "config.kubernetes.cpu-request"),
          "pods-max": _.get(this.model, "config.kubernetes.pods-max"),
          "pods-min": _.get(this.model, "config.kubernetes.pods-min"),
          namespace: _.get(this.model, "config.kubernetes.namespace"),
          registry: _.get(this.model, "config.kubernetes.registry"),
        },
        concurrency: {
          consumers: 10,
          "poll-timeout": 1000,
          "queue-size": 1000
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      }
    };
  }

  private createRedisCache(): {} {
    return {
      type: "redis-cache",
      name: "dfl-redis-cache",
      description: "Redis cache, created by standard wizard.",
      status: true,
      version: _.get(this.model, "kubernetes.container-image-version"),
      config: {
        redis: {
          "max-size": _.get(this.model, "config.redis.max-size"),
          ttl: _.get(this.model, "config.redis.ttl"),
          url: _.get(this.model, "config.redis.url"),
        },
        kafka: {
          "consumer-group": "dfl-redis-cache",
          "telemetry-topic": "esthesis-telemetry",
          "metadata-topic": "esthesis-metadata",
          "cluster-url": _.get(this.model, "config.kafka.cluster-url"),
          "jaas-config": _.get(this.model, "config.kafka.jaas-config"),
          "sasl-mechanism": _.get(this.model, "config.kafka.sasl-mechanism"),
          "security-protocol": _.get(this.model, "config.kafka.security-protocol"),
        },
        kubernetes: {
          "cpu-limit": _.get(this.model, "config.kubernetes.cpu-limit"),
          "cpu-request": _.get(this.model, "config.kubernetes.cpu-request"),
          "pods-max": _.get(this.model, "config.kubernetes.pods-max"),
          "pods-min": _.get(this.model, "config.kubernetes.pods-min"),
          namespace: _.get(this.model, "config.kubernetes.namespace"),
          registry: _.get(this.model, "config.kubernetes.registry"),
        },
        concurrency: {
          consumers: 10,
          "poll-timeout": 1000,
          "queue-size": 1000
        },
        logging: {
          common: "INFO",
          esthesis: "INFO"
        }
      }
    };
  }

  execute() {
    const calls: Array<Observable<any>> = [];
    const callNames: string[] = [];

    calls.push(this.dataflowService.save(this.createPingUpdaterCall()));
    callNames.push("Ping updater dataflow");
    calls.push(this.dataflowService.save(this.createCommandReplyUpdater()));
    callNames.push("Command Reply updater dataflow");
    calls.push(this.dataflowService.save(this.createMqttClient()));
    callNames.push("MQTT client dataflow");
    calls.push(this.dataflowService.save(this.createInfluxDBWriter()));
    callNames.push("InfluxDB writer dataflow");
    calls.push(this.dataflowService.save(this.createRedisCache()));
    callNames.push("Redis cache writer dataflow");

    // Make the calls.
    let counter = 0;
    from(calls)
    .pipe(
      concatMap((observable: Observable<any>) => {
        return observable.pipe(
          delay(1000),
          tap(() => {
            counter++;
            this.utilityService.popupSuccess(`Dataflow ${callNames[counter - 1]} started`);
          })
        );
      })
    )
    .subscribe({
      error: (error: any) => {
        this.utilityService.popupErrorWithTraceId("Failed to execute the wizard", error);
      },
      complete: () => {
        this.utilityService.popupSuccess("Wizard was executed successfully");
        this.router.navigate(["/dataflow"]);
      }
    });
  };
}
