# Initial configuration

The following sections describe how to configure the esthesis Core platform after installation.

## Create a Certificate Authority (CA)
- Navigate to "Key Management > CAs".
- Click on the "Create" button.
- Fill in all information except "Parent CA".

:::tip
esthesis Core can work without creating a CA. However, if in the future you want to enable
certificate-based authentication for your devices, you will need to create one then. Devices that
have already been registered with the platform prior to the creation of the CA will need to have
their certificates reissued. To avoid this hassle, we suggest you create a CA early on.
:::

## Create a tag
- Navigate to "Settings > Tags".
- Click on the "Create" button.
- Fill in all information.

:::tip
Tags in esthesis Core play an important role allowing components to be optimally grouped together.
It is not necessary to create a tag, but it is highly recommended.
:::

## Connect your MQTT server
- Navigate to "Integrations > Infrastructure".
- Click on the "Register" button.
- Fill in all information, choosing the tag you created before and setting the "State" to "Active".

## Tune the settings
- Navigate to "Settings > Settings".
- Under "Device Registration", choose the registration mode you want to use.
- Under "Security", choose as root CA the CA you created before.

## Set up the Dataflows
Without any dataflow configured, esthesis Core can not do much. You can set up individual
dataflows for your use case, or you can use one of the provided wizards. Once the dataflows are
configured, you can start registering devices and send data to the platform.
- Navigate to "Dataflows".
- Click on the "Create" button.
- Click on the "Wizards" button.
- Select the wizard you want to use and click on the "Configure" button.
- Fill in the details of the wizard and click on the "Execute" button.
