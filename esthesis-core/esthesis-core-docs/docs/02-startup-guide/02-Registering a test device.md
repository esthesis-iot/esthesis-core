# Registering a test device
Before you start registering real devices it is a good idea to test the platform with a test device.
You can register a test device and enable its "demo mode" to start receiving data right away.

## Prepare the platform for new devices registration
Check that esthesis Core is configured to accept new device registrations in
Settings > Device registration > Registration mode. You can set it to "Open registration" for now,
but do not forget to change it to a more secure setting later.

## Which agent client to use
esthesis Device Agent comes as a standalone executable natively compiled for different platform, as
well as a container image. You can choose the one that best fits your needs, however the native
executable does not require you to have access to a container runtime on your device.

## Download the agent client
You can download the device agent from:

[todo: add link to download page]

:::tip
Make sure you select the correct version for your platform.
:::

## Run the agent client
The agent client is a command line tool that can be heavily customised for your esthesis Core
installation. You can view all the available configuration options by issuing:

```bash
./esthesis-device-agent --help
```

For a typical installation, you can run the device agent as:

```bash
./esthesis-device-agent \
	--hardwareId "test-device-1" \
	--registrationUrl "http://{{URL}}/api/agent/v1/register" \
	--demoInterval 5
```

Replace `{{URL}}` with the URL of your esthesis Core installation.

## Check the device registration
If registration was successful, you should see the device in the device list in esthesis Core and
the device should be sending data to the platform:

![Demo device data](/img/docs/startup-guide/demo-device-data.png)
