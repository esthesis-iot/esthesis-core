# RDBMS Writer DFL

The RDBMS Writer DFL is responsible to update a relational database with incoming telemetry, and possibly metadata,
values. It obtains values from Kafka, after they have been passed all possible processing steps. Data is classified
on categories, with each category having one or more measurements. Categories and measurements are created automatically
once new data is discovered based on how you configure the DFL ([](#data-storage-strategy)).

The following RDBMSs are supported: MySQL, DB2, Derby, H2, MariaDB, MS SQL, Oracle, and PostgreSQL.

## Data storage strategy
The way in which the DFL updates the underlying RDBMS varies according to how you set up the DFL:
- Single strategy: The single strategy persists all incoming telemetry and metadata values in a single table. You can
define the name of the table, as well as the name of the columns used for storing the measurement name as well as the
measurement value. Use this strategy when you have a relative small number of different measurements how you want to
persist, as each measurement requires two columns. Do note that all columns should already exist on your table and
have the appropriate data type, the DFL will not create any columns for you.
- Multi strategy: The multi strategy persists all incoming telemetry and metadata values in multiple tables. The name
of the table is determined by the category of the measurement, as well as the name of the column holding the value. 

## Inputs

- RDBMS: The DFL requires the connection details to the RDBMS database, including the URL, the username, the password, and
  the database name.
- Kafka: The DFL obtains telemetry and metadata from Kafka. It requires Kafka connection details, as well as the topic
  names from which to read metadata and telemetry. You may leave empty either of those, if you do not need to process
  telemetry or metadata respectively.
- Kubernetes: The DFL is deployed as a Kubernetes deployment and requires the Kubernetes connection details.
- Concurrency: You can set the number of messages the DFL can locally queue for processing, the polling frequency, as
  well as the number of parallel execution processing threads.
- Logging: You can specify the logging level for the esthesis components in this DFL, as well as the general logging
  level.

## Outputs

The DFL updates a relational database with incoming telemetry and metadata values.