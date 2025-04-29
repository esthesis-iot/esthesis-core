# Dataflows

Dataflows (DFL) is a powerful feature of esthesis CORE, allowing to fully customise the data processing pipeline to your
exact needs. It is a way to define a sequence of operations that will be executed on the data before they are acquired, 
during the acquisition, and finally during persistence. 

![dfl-1.png](dfl-1.png)

The logical phases in which DFLs operate are:

- **DATA ACQUISITION**: During this phase remotely-collected data is first entered into esthesis CORE. The data sender
component can be the esthesis CORE agent installed on a remote device, or any other device following the esthesis CORE
[ELP protocol](esthesis-line-protocol.md). esthesis CORE provides a data acquisition DFL based on MQTT, which can
acquire data from an MQTT server.
- **DATA PROCESSING**: During this phase data already entered into esthesis CORE is further processed, analysed, etc.
esthesis CORE provides data processing DFLs to handle the ping/keep-alive responses of remote devices, to update 
replies to previously sent commands, to update a REDSI cache, etc.
- **DATA PERSISTENCE**: During this phase, processed data is stored in a persistence storage. esthesis CORE provides
data persistence DFLs for InfluxDB, a relational database, FIWARE Orion, etc.