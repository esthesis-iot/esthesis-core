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
### Ping updater
- Navigate to "Dataflows > Create" and choose "Ping updater".
