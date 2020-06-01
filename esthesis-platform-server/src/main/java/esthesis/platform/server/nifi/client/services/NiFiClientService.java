package esthesis.platform.server.nifi.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.platform.server.nifi.client.dto.EsthesisTemplateDTO;
import esthesis.platform.server.nifi.client.dto.EsthesisTemplateVersionDTO;
import esthesis.platform.server.nifi.client.dto.NiFiAboutDTO;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.dto.NiFiTemplateDTO;
import esthesis.platform.server.nifi.client.util.JacksonIgnoreInvalidFormatException;
import esthesis.platform.server.nifi.client.util.NifiConstants;
import esthesis.platform.server.nifi.client.util.NifiConstants.PATH;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.STATE;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NiFiClientService {

  @Autowired
  private NiFiClient niFiClient;
  //TODO use PATH_CODE_ROOT instead
  private final static String TEMPLATE_PREFIX = "esthesis_v";
  //TODO inject via Spring
  private static final ObjectMapper objectMapper = new ObjectMapper()
    .addHandler(new JacksonIgnoreInvalidFormatException());

  /**
   * Returns the version of the remote NiFi server.
   */
  public NiFiAboutDTO getAbout() throws IOException {
    final AboutEntity about = niFiClient.getAbout();
    return NiFiAboutDTO.builder()
      .title(about.getAbout().getTitle())
      .version(about.getAbout().getVersion())
      .build();

  }

  /**
   * Returns all templates available.
   */
  public Collection<NiFiTemplateDTO> getTemplates() throws IOException {
    Collection<NiFiTemplateDTO> templatesCollection = new ArrayList<>();
    final TemplatesEntity templates = niFiClient.getTemplates();
    for (TemplateEntity template : templates.getTemplates()) {
      templatesCollection.add(
        NiFiTemplateDTO.builder()
          .name(template.getTemplate().getName())
          .description(template.getTemplate().getDescription())
          .groupId(template.getTemplate().getGroupId())
          .id(template.getId())
          .build()
      );
    }

    return templatesCollection;
  }

  /**
   * Returns template matching given name.
   *
   * @param templateName The name of the wanted template.
   * @return An optional NiFiTemplateDTO containing the matching template or empty if not found.
   */
  public Optional<NiFiTemplateDTO> getTemplate(String templateName) throws IOException {
    return getTemplates().stream().filter(p -> p.getName().equals(templateName)).findFirst();
  }

  /**
   * An internal helper method to extract the esthesis template version information from the name of
   * a flow group following the esthesis template naming convention (i.e. esthesis_vX.Y.Z
   * [ESTHESIS]).
   *
   * @param flowGroupName The name of the flow group to extract the version from.
   */
  private static EsthesisTemplateVersionDTO extractEsthesisVersionFromFlowGroupName(
    String flowGroupName) {
    //TODO replace with a pattern matching instead?
    String version = StringUtils.substringAfter(flowGroupName, TEMPLATE_PREFIX);
    version = version.split(" ")[0];
    String[] versionElements = version.split("\\.");

    return new EsthesisTemplateVersionDTO(version, Integer.valueOf(versionElements[0]),
      Integer.valueOf(versionElements[1]), Integer.valueOf(versionElements[2]));
  }

  /**
   * Returns the list of esthesis templates already instantiated in NiFi. This list should normally
   * have a single entry, however in case a previous template installation went wrong you may end up
   * with more than one entry.
   *
   * Note that templates returned from this method have their templateId empty as it is not know
   * from which template a process group was instantiated.
   */
  public List<EsthesisTemplateDTO> getDeployedEsthesisTemplates() throws IOException {
    return niFiClient.getProcessGroups(getRootProcessGroupId()).getProcessGroupFlow().getFlow()
      .getProcessGroups().stream()
      .filter(
        processFlowGroup -> processFlowGroup.getComponent().getName()
          .startsWith(TEMPLATE_PREFIX))
      .map(processFlowGroup -> {
          EsthesisTemplateDTO esthesisTemplateDTO = new EsthesisTemplateDTO();
          esthesisTemplateDTO.setFlowGroupId(processFlowGroup.getId());
          esthesisTemplateDTO.setName(processFlowGroup.getComponent().getName());
          final EsthesisTemplateVersionDTO esthesisTemplateVersion =
            extractEsthesisVersionFromFlowGroupName(processFlowGroup.getComponent().getName());
          esthesisTemplateDTO.setVersionMajor(esthesisTemplateVersion.getMajor());
          esthesisTemplateDTO.setVersionMinor(esthesisTemplateVersion.getMinor());
          esthesisTemplateDTO.setVersionPatch(esthesisTemplateVersion.getPatch());
          return esthesisTemplateDTO;
        }
      )
      .collect(Collectors.toList());
  }

  /**
   * Returns the list of esthesis templates available NiFi.
   *
   * Note that templates returned from this method have the flowGroupId empty as it is not known
   * whether a template has been instantiated into one (ore more) flow groups.
   */
  public List<EsthesisTemplateDTO> getAvailableEsthesisTemplates() throws IOException {
    return niFiClient.getTemplates().getTemplates().stream()
      .filter(
        niFiTemplateDTO -> niFiTemplateDTO.getTemplate().getName().startsWith(TEMPLATE_PREFIX))
      .map(niFiTemplateDTO -> {
        EsthesisTemplateDTO esthesisTemplateDTO = new EsthesisTemplateDTO();
        esthesisTemplateDTO.setTemplateId(niFiTemplateDTO.getId());
        esthesisTemplateDTO.setName(niFiTemplateDTO.getTemplate().getName());
        final EsthesisTemplateVersionDTO esthesisTemplateVersion = extractEsthesisVersionFromFlowGroupName(
          niFiTemplateDTO.getTemplate().getName());
        esthesisTemplateDTO.setVersionMajor(esthesisTemplateVersion.getMajor());
        esthesisTemplateDTO.setVersionMinor(esthesisTemplateVersion.getMinor());
        esthesisTemplateDTO.setVersionPatch(esthesisTemplateVersion.getPatch());
        return esthesisTemplateDTO;
      })
      .collect(Collectors.toList());
  }

  /**
   * Uploads a template.
   *
   * @param templateResource The classpath resource to upload.
   */
  public NiFiTemplateDTO uploadTemplate(String templateResource) throws IOException {
    final TemplateEntity templateEntity = niFiClient.uploadTemplate(templateResource);

    return NiFiTemplateDTO.builder()
      .id(templateEntity.getTemplate().getId())
      .description(templateEntity.getTemplate().getDescription())
      .name(templateEntity.getTemplate().getName())
      .groupId(templateEntity.getTemplate().getGroupId())
      .build();
  }

  /**
   * Instantiates a template
   *
   * @param templateId The id of the template to instantiate.
   * @return true if created, false otherwise.
   */
  public boolean instantiateTemplate(String templateId) throws IOException {
    return niFiClient.instantiateTemplate(templateId) != null;
  }

  /**
   * Returns the Id of the root process group under which everything else resides.
   */
  private String getRootProcessGroupId() throws IOException {
    return niFiClient.getRootProcessGroup().getProcessGroupFlow().getId();
  }

  /**
   * Find the id of the process group containing the element under the requested path.
   */
  private String findProcessGroupId(PATH path) throws IOException {
    return niFiClient.findProcessGroup(NiFiSearchAlgorithm.NAME_ENDS_WITH, path.asList())
      .orElseThrow(() -> new IllegalArgumentException(
        MessageFormat.format("Could not find process for {0}.", path.asString()))).getId();
  }

  /**
   * Searches for given type of ControllerService in given ProcessGroup
   *
   * @param path The path to the group where the controller is located.
   * @param type The type of the ControllerService.
   * @return the id of the controller service if found, null otherwise.
   */
  public String findControllerServiceId(@NotNull PATH path, @NotNull String type)
    throws IOException {
    String parentProcessGroupId = findProcessGroupId(path);
    Optional<ControllerServiceEntity> controllerService = this.niFiClient
      .findControllerService(parentProcessGroupId, type);

    return controllerService.isPresent() ? controllerService.get().getId() : null;
  }

  /**
   * Changesthe state of all elements in given processor group.
   *
   * @param path The path of the group.
   * @param state The state all elements will be changed to.
   * @return The update state.
   */
  public String changeProcessorGroupState(@NotNull PATH path,
    @NotNull NifiConstants.Properties.Values.STATE state) throws IOException {
    String parentProcessGroupId = findProcessGroupId(path);

    final ScheduleComponentsEntity scheduleComponentsEntity =
      this.niFiClient.changeProcessorGroupState(parentProcessGroupId,
        state);
    return scheduleComponentsEntity.getState();
  }

  /**
   * Searches for an Output Port by name and parent group id.
   *
   * @param path The path where the output port is located.
   * @param name The name of the output port.
   * @return The id of the matching output port, null otherwise.
   */
  public String findOutputPortByName(PATH path, String name) throws IOException {
    Optional<ProcessGroupEntity> parentProcessGroup = niFiClient
      .findProcessGroup(NiFiSearchAlgorithm.NAME_ENDS_WITH,
        path.asList());

    return parentProcessGroup.isPresent() ? niFiClient
      .findOutputPortByName(
        parentProcessGroup.get().getId(), name).getComponent().getId() : null;
  }

  /**
   * Creates the SSL Context Controller service.
   *
   * @param name The name of the service.
   * @param keystoreFilename The fully-qualified filename of the Keystore.
   * @param keystorePassword The password for the Keystore.
   * @param truststoreFilename The fully-qualified filename of the Truststore.
   * @param truststorePassword The password for the Truststore.
   * @param path The path of the parent group where the service will be created.
   * @return The id of the newly created service.
   */
  public String createSSLContext(String name, String keystoreFilename, String keystorePassword,
    String truststoreFilename, String truststorePassword, PATH path)
    throws IOException {
    String parentProcessGroupId = findProcessGroupId(path);

    final ControllerServiceEntity controllerServiceEntity = niFiClient
      .createSSLContext(name, keystoreFilename, keystorePassword, truststoreFilename,
        truststorePassword, parentProcessGroupId);

    return controllerServiceEntity.getId();
  }

  /**
   * Updates an existing SSL Context Controller service.
   *
   * @param id The id of the existing service.
   * @param keystoreFilename The fully-qualified filename of the Keystore.
   * @param keystorePassword The password for the Keystore.
   * @param truststoreFilename The fully-qualified filename of the Truststore.
   * @param truststorePassword The password for the Truststore.
   * @return The id of the updated SSL Context service.
   */
  public String updateSSLContext(String id, String keystoreFilename, String keystorePassword,
    String truststoreFilename, String truststorePassword)
    throws IOException {

    final ControllerServiceEntity controllerServiceEntity = niFiClient
      .updateSSLContext(id, keystoreFilename, keystorePassword, truststoreFilename,
        truststorePassword);

    return controllerServiceEntity.getId();
  }

  /**
   * Creates a Database Connection Pool service.
   *
   * @param name The name of the service.
   * @param databaseConnectionURL The URL of the database to connect with.
   * @param databaseDriverClassName The Driver class name of the database.
   * @param databaseUser Database username.
   * @param password Database password for given user.
   * @param path The path of the parent group where the service will be created.
   * @return The id of the newly created Database Connection Pool service.
   */
  public String createDBConnectionPool(String name, String databaseConnectionURL,
    String databaseDriverClassName, String databaseDriverClassLocation, String databaseUser,
    String password,
    PATH path)
    throws IOException {
    String parentProcessGroupId = findProcessGroupId(path);

    final ControllerServiceEntity controllerServiceEntity = niFiClient
      .createDBCConnectionPool(name, databaseConnectionURL, databaseDriverClassName,
        databaseDriverClassLocation,
        databaseUser, password, parentProcessGroupId);

    return controllerServiceEntity.getId();
  }

  /**
   * Updates an existing Database Connection Pool Controller service.
   *
   * @param id The id of the existing service.
   * @param databaseConnectionURL The URL of the database to connect with.
   * @param databaseDriverClassName The Driver class name of the database.
   * @param databaseUser Database username.
   * @param password Database password for given user.
   * @return The id of the updated Database Connection Pool service.
   */
  public String updateDBCConnectionPool(String id, String databaseConnectionURL,
    String databaseDriverClassName, String databaseDriverClassLocation, String databaseUser,
    String password)
    throws IOException {

    final ControllerServiceEntity controllerServiceEntity = niFiClient
      .updateDBCConnectionPool(id, databaseConnectionURL, databaseDriverClassName,
        databaseDriverClassLocation,
        databaseUser, password);

    return controllerServiceEntity.getId();
  }

  public String createJsonTreeReader(String name, PATH path) throws IOException {
    String parentProcessGroupId = findProcessGroupId(path);

    return niFiClient.createJsonTreeReader(name, parentProcessGroupId).getId();
  }

  /**
   * Deletes a Controller service.
   *
   * @param id The id of the service that will be deleted.
   * @return The status of the deleted controller.
   */
  public String deleteController(String id) throws IOException {
    final ControllerServiceEntity controllerServiceEntity = niFiClient.deleteController(id);
    return controllerServiceEntity.getStatus().getRunStatus();
  }

  /**
   * Changes the status of a ControllerService.
   *
   * @param controllerServiceId the if of the ControllerService.
   * @param state The desired state of the ControllerService.
   * @return The updated state of the controller.
   */
  public String changeControllerServiceStatus(String controllerServiceId, STATE state)
    throws IOException {
    ControllerServiceEntity controllerServiceEntity = niFiClient
      .changeControllerServiceStatus(controllerServiceId, state);

    return controllerServiceEntity.getComponent().getState();
  }

  /**
   * Creates a ConsumeMQTT processor.
   *
   * @param name The name of the processor.
   * @param uri The URI to use to connect to the MQTT broker.
   * @param topic The MQTT topic filter to designate the topics to subscribe to.
   * @param qos The Quality of Service(QoS) to receive the message with.
   * @param queueSize Maximum number of messages this processor will hold in memory at one time.
   * @param sslContextServiceId the if of the SSL Context Service used to provide client certificate
   * information for TLS/SSL connections.
   * @param path The path of the parent group where the processor will be created.
   * @param outputPortName The name of the output port to connect.
   * @return The id of the newly created processor.
   */
  public String createConsumerMqtt(@NotNull String name, @NotNull String uri, @NotNull String topic,
    int qos, int queueSize, @Nullable String sslContextServiceId, @NotNull PATH path,
    @NotNull String outputPortName)
    throws IOException {
    // Find the group Id of the process group under which this reader will be created.
    String parentProcessGroupId = findProcessGroupId(path);

    // Create the consumer.
    final ProcessorEntity processorEntity = niFiClient
      .createConsumerMQTT(parentProcessGroupId, name, uri, topic, qos, queueSize,
        sslContextServiceId, outputPortName);

    return processorEntity.getId();
  }

  /**
   * Creates a ConsumeMQTT processor.
   *
   * @param name The name of the processor.
   * @param uri The URI to use to connect to the MQTT broker.
   * @param topic The MQTT topic filter to designate the topics to subscribe to.
   * @param qos The Quality of Service(QoS) to receive the message with.
   * @param queueSize Maximum number of messages this processor will hold in memory at one time.
   * @param path The path of the parent group where the processor will be created.
   * @param outputPortName The name of the output port to connect.
   * @return The id of the newly created processor.
   */
  public String createConsumerMqtt(String name, String uri, String topic, int qos, int queueSize,
    PATH path, String outputPortName) throws IOException {
    return createConsumerMqtt(name, uri, topic, qos, queueSize, null, path, outputPortName);
  }

  public String updateConsumerMQTT(String id, String uri, String topic, int qos, int queueSize)
    throws IOException {
    return niFiClient.updateConsumeMQTT(id, uri, topic, qos, queueSize).getId();
  }

  /**
   * Creates a PutInfluxDB processor.
   *
   * @param name The name of the processor.
   * @param dbName InfluxDB database to connect to.
   * @param url InfluxDB URL to connect to.
   * @param maxConnectionTimeoutSeconds The maximum time (in seconds) for establishing connection to
   * the InfluxDB.
   * @param username Username for accessing InfluxDB.
   * @param password Password for user.
   * @param charset Specifies the character set of the document data.
   * @param level InfluxDB consistency level.
   * @param retentionPolicy Retention policy for the saving the records.
   * @param maxRecordSize Maximum size of records allowed to be posted in one batch
   * @param maxRecordSizeUnit Unit for max record size.
   * @param path The path of the parent group where the processor will be created.
   * @param outputPortName The name of the output port to connect.
   * @param inputPortName The name of the input port to connect.
   * @return The id of the newly created processor.
   */
  public String createPutInfluxDB(@NotNull String name, @NotNull String dbName, @NotNull String url,
    int maxConnectionTimeoutSeconds, String username, String password, String charset,
    CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
    DATA_UNIT maxRecordSizeUnit, @NotNull PATH path, @NotNull String inputPortName,
    @NotNull String outputPortName) throws IOException {
    // Find the group Id of the process group under which this reader will be created.
    String parentProcessGroupId = findProcessGroupId(path);

    // Create the influx db writer.
    final ProcessorEntity processorEntity = niFiClient
      .createPutInfluxDB(parentProcessGroupId, name, dbName, url, maxConnectionTimeoutSeconds,
        username, password, charset, level, retentionPolicy, maxRecordSize, maxRecordSizeUnit
        , inputPortName, outputPortName);

    return processorEntity.getId();
  }

  public String updatePutInfluxDB(String id, @NotNull String dbName, @NotNull String url,
    int maxConnectionTimeoutSeconds, String username, String password, String charset,
    CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
    DATA_UNIT maxRecordSizeUnit) throws IOException {

    return niFiClient.updatePutInfluxDB(id, dbName, url, maxConnectionTimeoutSeconds, username,
      password, charset, level, retentionPolicy, maxRecordSize, maxRecordSizeUnit).getId();
  }

  /**
   * Creates a PutDatabaseRecord processor.
   *
   * @param name The name of the processor.
   * @param recordReaderId The id of the Controller Service to use for parsing incoming data and
   * determining the data's schema.
   * @param dcbpServiceId The id of the Controller Service that is used to obtain a connection to
   * the database for sending records.
   * @param statementType Specifies the type of SQL Statement to generate.
   * @param tableName The name of the table that the statement should affect.
   * @param path The path of the parent group where the processor will be created.
   * @param outputPortName The name of the output port to connect.
   * @return The id of the newly created processor.
   */
  public String createPutDatabaseRecord(
    @NotNull String name, @NotNull String recordReaderId, @NotNull String dcbpServiceId,
    @NotNull NifiConstants.Properties.Values.STATEMENT_TYPE statementType,
    @NotNull String tableName, @NotNull PATH path, @NotNull String inputPortName,
    @NotNull String outputPortName) throws IOException {
    // Configuration.
    String parentProcessGroupId = findProcessGroupId(path);

    // Create the database writer.
    final ProcessorEntity processorEntity = niFiClient
      .createPutDatabaseRecord(parentProcessGroupId, name, recordReaderId,
        dcbpServiceId, statementType, tableName, inputPortName, outputPortName);

    return processorEntity.getId();
  }

  public String updatePutDatabaseRecord(String processorId,
    @NotNull NifiConstants.Properties.Values.STATEMENT_TYPE statementType,
    @NotNull String tableName) throws IOException {

    return niFiClient.updatePutDatabaseRecord(processorId, statementType, tableName).getId();
  }

  /**
   * Changes the status of a Processor..
   *
   * @param processorId The id of the Processor.
   * @param state The desired state of the component.
   * @return The updated status of the processor.
   */
  public String changeProcessorStatus(String processorId, STATE state) throws IOException {
    return niFiClient.changeProcessorStatus(processorId, state).getStatus().getRunStatus();
  }

  /**
   * Deletes a processor. If the processor is the destination of a connection, the connection will
   * be also deleted.
   *
   * @param id The id of the processor to delete.
   * @return The state of the deleted processor.
   */
  public String deleteProcessor(String id) throws IOException {
    final ProcessorEntity processorEntity = niFiClient.deleteProcessor(id);
    return processorEntity.getComponent().getState();
  }

  public String getValidationErrors(String processorId) throws IOException {
    Collection<String> errors = niFiClient.getValidationErrors(processorId);
    String join = errors != null ? String.join("\n", errors) : "";
    return join;
  }
}
