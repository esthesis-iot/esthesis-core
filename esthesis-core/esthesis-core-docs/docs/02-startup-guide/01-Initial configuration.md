# Initial configuration

The following sections describe how to configure the esthesis Core platform after installation.

## Create a Certificate Authority
- Navigate to "Key Management > CAs".
- Click on the "Create" button.
- Fill in all information except "Parent CA".

## Create a platform Certificate
- Navigate to "Key Management > Certificates".
- Click on the "Create" button.
- Fill in all information by selecting the CA created in the previous step in the "Signed By" field.

## Create a tag
- Navigate to "Settings > Tags".
- Click on the "Create" button.
- Fill in all information.

## Connect your MQTT server
- Navigate to "Integrations > Infrastructure".
- Click on the "Register" button.
- Fill in all information, choosing the tag you created before and setting the "State" to "Active".

## Tune the settings
- Navigate to "Settings > Settings".
- Under "Device Registration", choose the registration mode you want to use.
- Under "Security", choose the platform certificate you created before.

## Set up the Dataflows
Without any dataflows configured, esthesis Core is just a nice UI. You can set up individual
dataflows for your use case, or you can use one of the provided wizards. Once the dataflows are
configured, you can start to register devices and send data to the platform.
- Navigate to "Dataflows".
- Click on the "Create" button.
- Click on the "Wizards" button.
- Select the wizard you want to use and click on the "Configure" button.
- Fill in the details of the wizard and click on the "Execute" button.
