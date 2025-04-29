# Hardware support

## Generic x86_64

## Arm64

## Raspberry Pi

### Raspberry Pi support and compatibility
The esthesis device agent supports Raspberry Pi devices, either by using the standalone executable,
or by using the Docker image. The following table shows the supported Raspberry Pi models and the
corresponding architecture:

| Model                             | Chip      | Standalone                        | Docker image |
|-----------------------------------|-----------|-----------------------------------|--------------|
| Raspberry Pi 1 Model A, A+, B, B+ | BCM2835   | esthesis-core-device-linux-arm6   | linux/arm/v6 |
| Raspberry Pi Zero, Zero W         | BCM2835   | esthesis-core-device-linux-arm6   | linux/arm/v6 |
| Raspberry Pi Compute Module 1		 	| BCM2835   | esthesis-core-device-linux-arm6   | linux/arm/v6 |
| Raspberry Pi 2 Model B            | BCM2836   | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi 2 Model B (late)     | BCM2837   | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi 3 Model B            | BCM2837   | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi Compute Module 3     | BCM2837   | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi 3 Model A+, B+       | BCM2837B0 | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi Compute Module 3+    | BCM2837B0 | esthesis-core-device-linux-arm7   | linux/arm/v7 |
| Raspberry Pi 4 Model B            | BCM2711   | esthesis-core-device-linux-arm64  | linux/arm64  |
| Raspberry Pi 400                  | BCM2711   | esthesis-core-device-linux-arm64  | linux/arm64  |
| Raspberry Pi Compute Module 4     | BCM2711   | esthesis-core-device-linux-arm64  | linux/arm64  |
| Raspberry Pi 5             			 	| BCM2712   | esthesis-core-device-linux-arm64  | linux/arm64  |

### A Guide to Setting Up Watchdog on Raspberry Pi

In the realm of Internet of Things (IoT), where devices communicate seamlessly to collect and exchange data, reliability is very important. Raspberry Pi serves as a versatile IoT gateway, facilitating the flow of information between devices and the cloud. However, ensuring continuous operation of the gateway is crucial for uninterrupted data flow. This guide focuses on simplifying the setup and configuration of watchdog on Raspberry Pi, enhancing its reliability as an IoT gateway and ensuring seamless data transmission in IoT ecosystems.

#### Activating the watchdog hardware

To start the process, we need to activate the watchdog hardware. By modifying the /boot/firmware/config.txt file, we enable the watchdog functionality on Raspberry Pi. This step ensures that the system can automatically reboot in case of unresponsiveness, a critical feature for IoT gateways.

```bash
sudo nano  /boot/firmware/config.txt
```

Add the following line at the end of the file:

```
# Enabling watchdog.
dtparam=watchdog=on
```

Once saved, reboot your Raspberry Pi. To confirm activation, list the watchdog devices:

```bash
ls -al /dev/watchdog*
```

#### Installing the watchdog

Now, let's install the watchdog software package. This step ensures that the necessary tools and services are in place to monitor and manage the watchdog functionality on Raspberry Pi.

Execute the following command to install watchdog:

```bash
sudo apt-get install watchdog
```

Verify the installation and necessary systemd files:

```bash
ls -l /lib/systemd/system/
```

Additionally, review the default configuration to ensure proper setup:

```bash
ls -al /etc/default/watchdog
```

#### Configuring the watchdog

With the watchdog installed, it's time to configure its behavior. By editing the configuration file, we define parameters such as maximum load, minimum memory usage, and the watchdog device to monitor.

Edit the configuration file:

```bash
nano /etc/watchdog.conf
```

Ensure the following lines are uncommented or added:

```
max-load-1 = 24
min-memory = 1
watchdog-device = /dev/watchdog
```

To prevent timeout errors, include:

```
watchdog-timeout=15
```

#### Starting/Monitoring the watchdog service

With the watchdog configured, let's start the service and monitor its status. This step ensures that the watchdog is actively monitoring the system for any signs of unresponsiveness.

Start the service:

```bash
sudo systemctl start watchdog
```

Check the status:

```bash
sudo systemctl status watchdog
```

Refer to the troubleshooting section for any timeout errors encountered during this step.

#### Adding watchdog on boot

Lastly, we ensure that the watchdog service starts automatically upon boot. This step guarantees continuous monitoring of the Raspberry Pi IoT gateway, even after a reboot or power cycle.

Edit the watchdog service file:

```bash
sudo nano /lib/systemd/system/watchdog.service
```

Add the following lines under the Install section:

```
[Install]
WantedBy=multi-user.target
```

Save the changes and enable watchdog on boot:

```bash
sudo systemctl enable watchdog
```
