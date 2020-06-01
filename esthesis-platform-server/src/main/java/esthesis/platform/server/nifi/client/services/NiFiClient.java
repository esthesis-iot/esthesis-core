package esthesis.platform.server.nifi.client.services;

import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_CHARSET;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_CONSISTENCY_LEVEL;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_DB_NAME;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_MAX_CONNECTION_TIMEOUT;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_MAX_RECORDS_SIZE;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_PASSWORD;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_RETENTION_POLICY;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_URL;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.INFLUX_USERNAME;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.PUT_DB_RECORD_DCBP_SERVICE;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.PUT_DB_RECORD_READER;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.PUT_DB_RECORD_STATEMENT_TYPE;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.PUT_DB_RECORD_TABLE_NAME;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.INPUT_PORT;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.OUTPUT_PORT;
import static esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.PROCESSOR;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import esthesis.platform.server.nifi.client.dto.CallReplyDTO;
import esthesis.platform.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.server.nifi.client.exception.NiFiProcessingException;
import esthesis.platform.server.nifi.client.util.JacksonIgnoreInvalidFormatException;
import esthesis.platform.server.nifi.client.util.NifiConstants;
import esthesis.platform.server.nifi.client.util.NifiConstants.Bundle.BundleArtifact;
import esthesis.platform.server.nifi.client.util.NifiConstants.Bundle.BundleGroup;
import esthesis.platform.server.nifi.client.util.NifiConstants.ControllerService.Type;
import esthesis.platform.server.nifi.client.util.NifiConstants.Processor;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.RELATIONSHIP_TYPE;
import esthesis.platform.server.nifi.client.util.NifiConstants.Properties.Values.STATE;
import esthesis.platform.server.nifi.client.util.Util;
import esthesis.platform.server.service.NiFiService;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BundleDTO;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceRunStatusEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.FlowEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.InstantiateTemplateRequestEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupFlowEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProcessorRunStatusEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log
@Component
public class NiFiClient {

  //TODO: Both variables next, will be fetched by the NiFiConnector services of esthesis
  // (NiFiConnector should cache these values whenever a NiFi server is registered/edited).
  private static final String NIFI_VERSION = "1.10.0";

  private static final ObjectMapper mapper = new ObjectMapper()
    .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .addHandler(new JacksonIgnoreInvalidFormatException());
  // The HTTP client to use when making calls.
  private final OkHttpClient client;
  private final ObjectMapper xmlMapper;

  private enum HTTP_METHOD {PUT, POST}

  @Autowired
  private NiFiService niFiService;

  public NiFiClient() {
    client = new OkHttpClient();
    xmlMapper = new XmlMapper()
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .addHandler(new JacksonIgnoreInvalidFormatException());
  }

  private String getNiFiUrl() {
    return niFiService.getActiveNiFi().getUrl() + "/nifi-api";
  }

  //  private OkHttpClient secureClient()
  //      throws KeyStoreException, CertificateException, NoSuchAlgorithmException,
  //      IOException, UnrecoverableKeyException, KeyManagementException, IllegalStateException {
  //
  //    ClassLoader classLoader = getClass().getClassLoader();
  //    InputStream inputStream = classLoader.getResourceAsStream("client-certificate.p12);
  //
  //    final TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
  //      @Override
  //      public X509Certificate[] getAcceptedIssuers() {
  //        X509Certificate[] x509Certificates = new X509Certificate[0];
  //        return x509Certificates;
  //      }
  //
  //      @Override
  //      public void checkServerTrusted(final X509Certificate[] chain,
  //          final String authType) {
  //        System.out.println(": authType: " + String.valueOf(authType));
  //      }
  //
  //      @Override
  //      public void checkClientTrusted(final X509Certificate[] chain,
  //          final String authType) {
  //        System.out.println(": authType: " + String.valueOf(authType));
  //      }
  //    }};
  //
  //    X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
  //
  //    KeyStore clientStore = KeyStore.getInstance("PKCS12");
  //    clientStore.load(inputStream, "client-certificate-pswd".toCharArray());
  //
  //    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
  //    kmf.init(clientStore, "client-certificate-pswd".toCharArray());
  //    KeyManager[] kms = kmf.getKeyManagers();
  //
  //    SSLContext sslContext = SSLContext.getInstance("TLS");
  //    sslContext.init(kms, new TrustManager[]{trustManager}, null);
  //    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
  //
  //    return new OkHttpClient().newBuilder().sslSocketFactory(sslSocketFactory, trustManager).build();
  //  }

  private CallReplyDTO prepareReply(Response response) throws IOException {
    CallReplyDTO callReplyDTO = new CallReplyDTO();
    callReplyDTO.setCode(response.code());
    callReplyDTO.setSuccessful(response.isSuccessful());
    if (response.body() != null) {
      callReplyDTO.setBody(response.body().string());
    }

    return callReplyDTO;
  }

  /**
   * A helper method to execute a GET call to NiFi.
   */
  private CallReplyDTO getCall(String context) throws IOException {
    Request request = new Request.Builder()
      .url(getNiFiUrl() + context)
      .build();

    try (Response response = client.newCall(request).execute()) {
      return prepareReply(response);
    }
  }

  /**
   * A helper method to execute a DELETE call to NiFi
   */
  private CallReplyDTO deleteCall(String context) throws IOException {
    Request request = new Request.Builder()
      .url(getNiFiUrl() + context)
      .delete()
      .build();

    try (Response response = client.newCall(request).execute()) {
      return prepareReply(response);
    }
  }

  /**
   * A helper method to execute a POST call to NiFi using a multipart body.
   *
   * @param context The context on which this request will be posted to.
   * @param body The form's keys and values to send.
   */
  private CallReplyDTO postFormCall(String context, Map<String, Object> body)
    throws IOException {
    // Add form parameters.
    final Builder builder = new Builder().setType(MultipartBody.FORM);
    body.keySet().forEach(key -> builder.addFormDataPart(key, body.get(key).toString()));

    // Prepare POST call.
    Request request = new Request.Builder()
      .url(getNiFiUrl() + context)
      .post(builder.build())
      .build();

    // Return reply.
    try (Response response = client.newCall(request).execute()) {
      return prepareReply(response);
    }
  }

  /**
   * A helper method to execute a POST or PUT call to NiFi using a JSON body.
   *
   * @param context The context on which this request will be posted to.
   * @param jsonBody The body is JSON format
   * @param httpMethod The method type.
   */
  private CallReplyDTO jsonCall(String context, Object jsonBody, HTTP_METHOD httpMethod)
    throws IOException {

    // Add JSON payload.
    RequestBody body = RequestBody.create(mapper.writeValueAsString(jsonBody),
      MediaType.parse("application/json; charset=utf-8"));

    // Prepare POST call.
    final Request.Builder requestBuilder = new Request.Builder().url(getNiFiUrl() + context);
    switch (httpMethod) {
      case POST:
        requestBuilder.post(body);
        break;
      case PUT:
        requestBuilder.put(body);
        break;
    }

    // Return reply.
    try (Response response = client.newCall(requestBuilder.build()).execute()) {
      return prepareReply(response);
    }
  }

  /**
   * A helper method to execute a POST call to NiFi using a JSON body.
   *
   * @param context The context on which this request will be posted to.
   * @param jsonBody The body is JSON format
   */
  private CallReplyDTO postJSONCall(String context, Object jsonBody) throws IOException {
    return jsonCall(context, jsonBody, HTTP_METHOD.POST);
  }

  /**
   * A helper method to execute a PUT call to NiFi using a JSON body.
   *
   * @param context The context on which this request will be posted to.
   * @param jsonBody The body is JSON format
   */
  private CallReplyDTO putJSONCall(String context, Object jsonBody) throws IOException {
    return jsonCall(context, jsonBody, HTTP_METHOD.PUT);
  }

  /**
   * Returns the version of the remote NiFi server.
   */
  public AboutEntity getAbout() throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/flow/about");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), AboutEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  //  public AccessStatusEntity getAccessStatus()
  //      throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException {
  //    CallReplyDTO callReplyDTO = getSSLCall("/access");
  //    if (callReplyDTO.isSuccessful()) {
  //      return mapper.readValue(callReplyDTO.getBody(), AccessStatusEntity.class);
  //    } else {
  //      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
  //    }
  //  }

  /**
   * Uploads a template.
   *
   * @param templateResource The classpath resource to upload.
   */
  public TemplateEntity uploadTemplate(String templateResource) throws IOException {
    String rootProcessGroupId = getRootProcessGroup().getProcessGroupFlow().getId();
    String templateBody = Util.readFromClasspath(templateResource);

    final CallReplyDTO callReplyDTO = postFormCall(
      "/process-groups/" + rootProcessGroupId + "/templates/upload",
      ImmutableMap.of("template", templateBody));

    if (callReplyDTO.isSuccessful()) {
      return xmlMapper.readValue(callReplyDTO.getBody(), TemplateEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Instantiates a template
   *
   * @param templateId The id of the template to instantiate.
   * @return a FlowEntity representing the template.
   */
  public FlowEntity instantiateTemplate(String templateId) throws IOException {
    String rootProcessGroupId = getRootProcessGroup().getProcessGroupFlow().getId();
    InstantiateTemplateRequestEntity request = new InstantiateTemplateRequestEntity();
    request.setTemplateId(templateId);
    request.setOriginX(100d);
    request.setOriginY(100d);

    final CallReplyDTO callReplyDTO = postJSONCall(
      "/process-groups/" + rootProcessGroupId + "/template-instance", request);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), FlowEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Returns all templates available.
   */
  public TemplatesEntity getTemplates() throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/flow/templates");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), TemplatesEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Returns the Id of the root process group under which everything else resides.
   */
  public ProcessGroupFlowEntity getRootProcessGroup() throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/flow/process-groups/root");
    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessGroupFlowEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Finds the process groups under a parent process group Id.
   *
   * @param parentProcessGroupId The parent process group Id.
   */
  public ProcessGroupFlowEntity getProcessGroups(String parentProcessGroupId) throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/flow/process-groups/" + parentProcessGroupId);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessGroupFlowEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Searches for a process group.
   *
   * @param niFiSearchAlgorithm The searching algorithm used.
   * @param searchPath The path of the wanted ProcessGroup.
   * @return An optional including the found group, empty if nothing is found.
   */
  public Optional<ProcessGroupEntity> findProcessGroup(NiFiSearchAlgorithm niFiSearchAlgorithm,
    List<String> searchPath)
    throws IOException {
    Optional<ProcessGroupEntity> processGroupEntity = Optional.empty();
    String rootProcessGroupId = getRootProcessGroup().getProcessGroupFlow().getId();
    for (String path : searchPath) {
      final ProcessGroupFlowEntity processGroupFlowEntity =
        getProcessGroups(processGroupEntity.isPresent() ? processGroupEntity.get().getId()
          : rootProcessGroupId);
      final Optional<ProcessGroupEntity> match = processGroupFlowEntity.getProcessGroupFlow()
        .getFlow().getProcessGroups().stream().filter(p -> {
          switch (niFiSearchAlgorithm) {
            case NAME_ENDS_WITH:
              return p.getComponent().getName().endsWith(path);
            case NAME_EQUALS:
              return p.getComponent().getName().equals(path);
            default:
              return false;
          }
        }).findFirst();
      if (match.isPresent()) {
        processGroupEntity = Optional.of(match.get());
      } else {
        break;
      }
    }

    return processGroupEntity;
  }

  /**
   * Searches for given type of ControllerService in given ProcessGroup
   *
   * @param parentProcessGroupId The id of the ProcessGroup where the service is located.
   * @param type The type of the ControllerService.
   * @return An Optional containing the first ControllerService matching the given criteria, empty
   * if nothing matches.
   */
  public Optional<ControllerServiceEntity> findControllerService(String parentProcessGroupId,
    String type)
    throws IOException {
    CallReplyDTO callReplyDTO = getCall(
      "/flow/process-groups/" + parentProcessGroupId + "/controller-services");

    Optional<ControllerServiceEntity> match = Optional.empty();

    if (callReplyDTO.isSuccessful()) {
      ControllerServicesEntity controllerServicesEntity = mapper
        .readValue(callReplyDTO.getBody(), ControllerServicesEntity.class);
      Optional<ControllerServiceEntity> controllerByType =
        controllerServicesEntity.getControllerServices()
          .stream().filter(
          controllerServiceEntity ->
            controllerServiceEntity.getComponent().getType().equals(type))
          .findFirst();
      if (controllerByType.isPresent()) {
        match = controllerByType;
      }
      return match;
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

  }

  /**
   * Changes the state of a ProcessorGroup.
   *
   * @param processGroupId The id of the ProcessorGroup.
   * @param state The desired state of the descendant components.
   * @return ScheduleComponentsEntity
   */
  public ScheduleComponentsEntity changeProcessorGroupState(String processGroupId,
    STATE state)
    throws IOException {
    ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
    scheduleComponentsEntity.setId(processGroupId);
    scheduleComponentsEntity.setState(state.name());

    final CallReplyDTO callReplyDTO = putJSONCall("/flow/process-groups/" + processGroupId,
      scheduleComponentsEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ScheduleComponentsEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Searches for an Input Port by name and parent group id.
   *
   * @param parentProcessGroupId The id of the group where the input port is located.
   * @param name The name of the input port.
   * @return PortEntity object containing the wanted input port.
   */
  public PortEntity findInputPortByName(String parentProcessGroupId, String name)
    throws IOException {
    final CallReplyDTO callReplyDTO = getCall(
      "/process-groups/" + parentProcessGroupId + "/input-ports");

    if (callReplyDTO.isSuccessful()) {
      InputPortsEntity inputPortsEntity = mapper
        .readValue(callReplyDTO.getBody(), InputPortsEntity.class);
      Optional<PortEntity> optionalPortEntity = inputPortsEntity.getInputPorts().stream()
        .filter(portEntity -> portEntity.getComponent().getName().equals(name)).findFirst();
      return optionalPortEntity.get();
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }


  /**
   * Searches for an Output Port by name and parent group id.
   *
   * @param parentProcessGroupId The id of the group where the output port is located.
   * @param name The name of the output port.
   * @return PortEntity object containing the wanted output port.
   */
  public PortEntity findOutputPortByName(String parentProcessGroupId, String name)
    throws IOException {
    final CallReplyDTO callReplyDTO = getCall(
      "/process-groups/" + parentProcessGroupId + "/output-ports");

    if (callReplyDTO.isSuccessful()) {
      OutputPortsEntity outputPortsEntity = mapper
        .readValue(callReplyDTO.getBody(), OutputPortsEntity.class);
      Optional<PortEntity> optionalPortEntity = outputPortsEntity.getOutputPorts().stream()
        .filter(portEntity -> portEntity.getComponent().getName().equals(name)).findFirst();
      return optionalPortEntity.get();
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Creates the SSL Context Controller service.
   *
   * @param name The name of the service.
   * @param keystoreFilename The fully-qualified filename of the Keystore.
   * @param keystorePassword The password for the Keystore.
   * @param truststoreFilename The fully-qualified filename of the Truststore.
   * @param truststorePassword The password for the Truststore.
   * @param parentProcessGroupId The id of the parent group where the service will be created.
   * @return The ControlServiceEntity containing the newly created SSL Context service.
   */
  public ControllerServiceEntity createSSLContext(String name, String keystoreFilename,
    String keystorePassword,
    String truststoreFilename, String truststorePassword, String parentProcessGroupId)
    throws IOException {

    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.SSL_CONTEXT);
    ControllerServiceDTO controllerServiceDTO = createControllerServiceDTO(
      Type.SSL_CONTEXT, name, bundleDTO);

    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.KEYSTORE_FILENAME, keystoreFilename);
    properties.put(Properties.KEYSTORE_PSWD, keystorePassword);
    properties.put(Properties.KEYSTORE_TYPE, "PKCS12");
    properties.put(Properties.TRUSTSTORE_FILENAME, truststoreFilename);
    properties.put(Properties.TRUSTSTORE_PSWD, truststorePassword);
    properties.put(Properties.TRUSTSTORE_TYPE, "PKCS12");
    properties.put(Properties.SSL_PROTOCOL, "TLSv1.2");

    controllerServiceDTO.setProperties(properties);

    return createController(parentProcessGroupId, controllerServiceDTO);
  }

  /**
   * Updates an existing SSL Context Controller service.
   *
   * @param id The id of the existing service.
   * @param keystoreFilename The fully-qualified filename of the Keystore.
   * @param keystorePassword The password for the Keystore.
   * @param truststoreFilename The fully-qualified filename of the Truststore.
   * @param truststorePassword The password for the Truststore.
   * @return The ControlServiceEntity containing the updated SSL Context service.
   */
  public ControllerServiceEntity updateSSLContext(String id, String keystoreFilename,
    String keystorePassword, String truststoreFilename, String truststorePassword)
    throws IOException {

    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.KEYSTORE_FILENAME, keystoreFilename);
    properties.put(Properties.KEYSTORE_PSWD, keystorePassword);
    properties.put(Properties.KEYSTORE_TYPE, "PKCS12");
    properties.put(Properties.TRUSTSTORE_FILENAME, truststoreFilename);
    properties.put(Properties.TRUSTSTORE_PSWD, truststorePassword);
    properties.put(Properties.TRUSTSTORE_TYPE, "PKCS12");
    properties.put(Properties.SSL_PROTOCOL, "TLSv1.2");

    return updateController(id, properties);
  }

  /**
   * Creates a Database Connection Pool service.
   *
   * @param name The name of the service.
   * @param databaseConnectionURL The URL of the database to connect with.
   * @param databaseDriverClassName The Driver class name of the database.
   * @param databaseUser Database username.
   * @param password Database password for given user.
   * @param parentProcessGroupId The id of the parent group where the service will be created.
   * @return The ControlServiceEntity containing the newly created Database Connection Pool service.
   */
  public ControllerServiceEntity createDBCConnectionPool(String name, String databaseConnectionURL,
    String databaseDriverClassName, String databaseDriverClassLocation,
    String databaseUser, String password,
    String parentProcessGroupId) throws IOException {

    BundleDTO bundleDTO = createBundleDTO(
      BundleGroup.NIFI,
      BundleArtifact.DBCP);
    ControllerServiceDTO controllerServiceDTO = createControllerServiceDTO(
      Type.DBCP_POOL,
      name, bundleDTO);

    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.DB_CONNECTION_URL, databaseConnectionURL);
    properties.put(Properties.DB_DRIVER_CLASS_NAME, databaseDriverClassName);
    properties.put(Properties.DB_DRIVER_LOCATION, databaseDriverClassLocation);
    properties.put(Properties.DB_USER, databaseUser);
    properties.put(Properties.PSWD, password);

    controllerServiceDTO.setProperties(properties);

    return createController(parentProcessGroupId, controllerServiceDTO);
  }

  /**
   * Updates an existing Database Connection Pool Controller service.
   *
   * @param id The id of the existing service.
   * @param databaseConnectionURL The URL of the database to connect with.
   * @param databaseDriverClassName The Driver class name of the database.
   * @param databaseUser Database username.
   * @param password Database password for given user.
   * @return The ControlServiceEntity containing the updated Database Connection Pool service.
   */
  public ControllerServiceEntity updateDBCConnectionPool(String id, String databaseConnectionURL,
    String databaseDriverClassName, String databaseDriverClassLocation,
    String databaseUser, String password) throws IOException {

    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.DB_CONNECTION_URL, databaseConnectionURL);
    properties.put(Properties.DB_DRIVER_CLASS_NAME, databaseDriverClassName);
    properties.put(Properties.DB_DRIVER_LOCATION, databaseDriverClassLocation);
    properties.put(Properties.DB_USER, databaseUser);
    properties.put(Properties.PSWD, password);

    return updateController(id, properties);
  }

  public ControllerServiceEntity createJsonTreeReader(String name, String parentProcessGroupId)
    throws IOException {
    BundleDTO bundleDTO = createBundleDTO(
      BundleGroup.NIFI,
      BundleArtifact.RECORD_SERIALIZATION);
    ControllerServiceDTO controllerServiceDTO = createControllerServiceDTO(
      Type.JSON_TREE_READER,
      name, bundleDTO);

    return createController(parentProcessGroupId, controllerServiceDTO);
  }


  /**
   * Helper method, used to create a Controller.
   *
   * @param parentProcessGroupId The id of the parent group where the service will be created.
   * @param controllerServiceDTO A ControllerServiceDTO containing all needed values for the
   * creation.
   * @return The ControlServiceEntity containing the newly created controller.
   */
  private ControllerServiceEntity createController(String parentProcessGroupId,
    ControllerServiceDTO controllerServiceDTO)
    throws IOException {

    RevisionDTO revisionDTO = createRevisionDTO();
    ControllerServiceEntity controllerServiceEntity = new ControllerServiceEntity();
    controllerServiceEntity.setRevision(revisionDTO);
    controllerServiceEntity.setComponent(controllerServiceDTO);
    CallReplyDTO callReplyDTO = postJSONCall(
      "/process-groups/" + parentProcessGroupId + "/controller-services",
      controllerServiceEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Helper method, used to update a Controller.
   *
   * @param id The id of the controller that will be updated.
   * @param properties A key-value map with new properties. creation.
   * @return The ControlServiceEntity containing the updated controller.
   */
  private ControllerServiceEntity updateController(String id, Map<String, String> properties)
    throws IOException {

    changeControllerServiceStatus(id, STATE.DISABLED);

    ControllerServiceEntity controllerServiceEntity;
    CallReplyDTO callReplyDTO = getCall("/controller-services/" + id);

    if (callReplyDTO.isSuccessful()) {
      controllerServiceEntity = mapper
        .readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

    ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
    controllerServiceDTO.setId(id);
    controllerServiceDTO.setProperties(properties);

    RevisionDTO revisionDTO = createRevisionDTO();
    revisionDTO.setClientId(id);
    revisionDTO.setVersion(controllerServiceEntity.getRevision().getVersion());

    ControllerServiceEntity updatedEntity = new ControllerServiceEntity();
    updatedEntity.setRevision(revisionDTO);
    updatedEntity.setComponent(controllerServiceDTO);

    callReplyDTO = putJSONCall("/controller-services/" + id, updatedEntity);

    if (callReplyDTO.isSuccessful()) {
      changeControllerServiceStatus(id, STATE.ENABLED);
      return mapper.readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Deletes a Controller service.
   *
   * @param id The id of the service that will be deleted.
   * @return A ControllerServiceEntity containing the deleted service.
   */
  public ControllerServiceEntity deleteController(String id) throws IOException {
    //Disabling controller before deleting
    ControllerServiceEntity controllerServiceEntity = this
      .changeControllerServiceStatus(id, STATE.DISABLED);

    CallReplyDTO callReplyDTO =
      deleteCall(
        "/controller-services/" + id + "?version=" + controllerServiceEntity.getRevision()
          .getVersion() +
          "&clientId=" + id
          + "&disconnectedNodeAcknowledged=false");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Changes the status of a ControllerService.
   *
   * @param controllerServiceId the if of the ControllerService.
   * @param state The desired state of the ControllerService.
   * @return ControllerServiceEntity
   */
  public ControllerServiceEntity changeControllerServiceStatus(String controllerServiceId,
    STATE state)
    throws IOException {

    CallReplyDTO getControllerCallReplyDTO = getCall("/controller-services/" + controllerServiceId);

    if (getControllerCallReplyDTO.isSuccessful()) {
      ControllerServiceEntity controllerServiceEntity = mapper
        .readValue(getControllerCallReplyDTO.getBody(), ControllerServiceEntity.class);
      ControllerServiceRunStatusEntity controllerServiceRunStatusEntity =
        new ControllerServiceRunStatusEntity();

      controllerServiceRunStatusEntity.setRevision(controllerServiceEntity.getRevision());
      controllerServiceRunStatusEntity.setState(state.name());

      CallReplyDTO callReplyDTO = putJSONCall("/controller-services/" + controllerServiceId + "/run"
        + "-status", controllerServiceRunStatusEntity);
      if (callReplyDTO.isSuccessful()) {
        return mapper.readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
      } else {
        throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
      }
    } else {
      throw new NiFiProcessingException(getControllerCallReplyDTO.getBody(),
        getControllerCallReplyDTO.getCode());
    }
  }

  /**
   * Creates a ConsumeMQTT processor.
   *
   * @param parentProcessGroupId The id of the parent group where the processor will be created.
   * @param name The name of the processor.
   * @param uri The URI to use to connect to the MQTT broker.
   * @param topic The MQTT topic filter to designate the topics to subscribe to.
   * @param qos The Quality of Service(QoS) to receive the message with.
   * @param queueSize Maximum number of messages this processor will hold in memory at one time.
   * @param sslContextServiceId the if of the SSL Context Service used to provide client certificate
   * information for TLS/SSL connections.
   * @param outputPortName The name of the output port to connect.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createConsumerMQTT(@NotNull String parentProcessGroupId,
    @NotNull String name,
    @NotNull String uri, @NotNull String topic, int qos, int queueSize,
    @Nullable String sslContextServiceId, String outputPortName)
    throws IOException {

    // Configuration.
    Map<String, String> properties = setConsumeMQTTProperties(uri, topic, qos, queueSize);
    properties.put(Properties.CLIENT_ID, name + " [NIFI] ");
    if (StringUtils.isNotBlank(sslContextServiceId)) {
      properties.put(Properties.SSL_CONTEXT_SERVICE, sslContextServiceId);
    }

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.MQTT);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.MQTT_CONSUME,
      name, processorConfigDTO, bundleDTO);

    Set<String> relationshipTypes =
      new HashSet<>(Arrays.asList(RELATIONSHIP_TYPE.MESSAGE.getType()));
    return createProcessor(parentProcessGroupId, processorDTO, relationshipTypes, outputPortName);
  }

  public ProcessorEntity updateConsumeMQTT(String id, @NotNull String uri, @NotNull String topic,
    int qos,
    int queueSize) throws IOException {
    Map<String, String> properties = setConsumeMQTTProperties(uri, topic, qos, queueSize);
    return updateProcessor(id, properties);
  }

  private Map<String, String> setConsumeMQTTProperties(String uri, @NotNull String topic, int qos,
    int queueSize) {
    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.BROKER_URI, uri);
    properties.put(Properties.MAX_QUEUE_SIZE, String.valueOf(queueSize));
    properties.put(Properties.TOPIC_FILTER, topic);
    properties.put(Properties.QOS, String.valueOf(qos));

    return properties;
  }

  private void createUpstreamConnection(ProcessorEntity processorEntity,
    String parentProcessGroupId,
    String inputPortName) throws IOException {

    PortEntity inputPort = findInputPortByName(parentProcessGroupId, inputPortName);

    ConnectionEntity connectionEntity = new ConnectionEntity();
    ConnectionDTO connectionDTO = new ConnectionDTO();

    ConnectableDTO sourceDTO = new ConnectableDTO();
    sourceDTO.setGroupId(parentProcessGroupId);
    sourceDTO.setType(String.valueOf(INPUT_PORT));
    sourceDTO.setId(inputPort.getId());

    ConnectableDTO destinationDTO = new ConnectableDTO();
    destinationDTO.setGroupId(parentProcessGroupId);
    destinationDTO.setType(String.valueOf(PROCESSOR));
    destinationDTO.setId(processorEntity.getId());

    connectionDTO.setSource(sourceDTO);
    connectionDTO.setDestination(destinationDTO);
    connectionEntity.setComponent(connectionDTO);
    connectionEntity.setRevision(createRevisionDTO());

    CallReplyDTO callReplyDTO = postJSONCall(
      "/process-groups/" + parentProcessGroupId + "/connections", connectionEntity);
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Creates a PutInfluxDB processor.
   *
   * @param parentProcessGroupId The id of the parent group where the processor will be created.
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
   * @param outputPortName The name of the output port to connect.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createPutInfluxDB(@NotNull String parentProcessGroupId,
    @NotNull String name, @NotNull String dbName, @NotNull String url,
    int maxConnectionTimeoutSeconds, String username, String password, String charset,
    CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
    DATA_UNIT maxRecordSizeUnit, String inputPortName, String outputPortName) throws IOException {

    // Configuration.
    Map<String, String> properties = setPutInfluxDBProperties(dbName, url,
      maxConnectionTimeoutSeconds, username, password, charset, level, retentionPolicy,
      maxRecordSize, maxRecordSizeUnit);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
      Arrays.asList(RELATIONSHIP_TYPE.SUCCESS.getType())));
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.INFLUX_DB);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_INFLUX_DB,
      name, processorConfigDTO, bundleDTO);
    Set<String> relationshipTypes = new HashSet<>(Arrays.asList(
      RELATIONSHIP_TYPE.FAILURE.getType(),
      RELATIONSHIP_TYPE.RETRY.getType(),
      RELATIONSHIP_TYPE.FAILURE_MAX_SIZE.getType()
    ));
    ProcessorEntity processor = createProcessor(parentProcessGroupId, processorDTO,
      relationshipTypes, outputPortName);

    createUpstreamConnection(processor, parentProcessGroupId, inputPortName);
    return processor;
  }

  public ProcessorEntity updatePutInfluxDB(String processorId,
    @NotNull String dbName,
    @NotNull String url,
    int maxConnectionTimeoutSeconds, String username, String password, String charset,
    CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
    DATA_UNIT maxRecordSizeUnit) throws IOException {

    // Configuration.
    Map<String, String> properties = setPutInfluxDBProperties(dbName, url,
      maxConnectionTimeoutSeconds, username, password, charset, level, retentionPolicy,
      maxRecordSize, maxRecordSizeUnit);

    return updateProcessor(processorId, properties);

  }

  private Map<String, String> setPutInfluxDBProperties(@NotNull String dbName,
    @NotNull String url,
    int maxConnectionTimeoutSeconds, String username, String password, String charset,
    CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
    DATA_UNIT maxRecordSizeUnit) {

    // Configuration.
    Map<String, String> properties = new HashMap<>();
    properties.put(INFLUX_DB_NAME, dbName);
    properties.put(INFLUX_URL, url);
    properties.put(INFLUX_MAX_CONNECTION_TIMEOUT, maxConnectionTimeoutSeconds + " seconds");
    properties.put(INFLUX_USERNAME, username);
    properties.put(INFLUX_PASSWORD, password);
    properties.put(INFLUX_CHARSET, charset);
    properties.put(INFLUX_CONSISTENCY_LEVEL, String.valueOf(level));
    properties.put(INFLUX_RETENTION_POLICY, retentionPolicy);
    properties.put(INFLUX_MAX_RECORDS_SIZE, maxRecordSize + " " + maxRecordSizeUnit);

    return properties;
  }

  /**
   * Creates a PutDatabaseRecord processor.
   *
   * @param parentProcessGroupId The id of the parent group where the processor will be created.
   * @param name The name of the processor.
   * @param recordReaderId The id of the Controller Service to use for parsing incoming data and
   * determining the data's schema.
   * @param statementType Specifies the type of SQL Statement to generate.
   * @param dcbpServiceId The id of the Controller Service that is used to obtain a connection to
   * the database for sending records.
   * @param tableName The name of the table that the statement should affect.
   * @param outputPortName The name of the output port to connect.
   * @param inputPortName The name of the input port to connect.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createPutDatabaseRecord(@NotNull String parentProcessGroupId,
    @NotNull String name, @NotNull String recordReaderId, @NotNull String dcbpServiceId,
    @NotNull NifiConstants.Properties.Values.STATEMENT_TYPE statementType,
    @NotNull String tableName,
    @NotNull String inputPortName, @NotNull String outputPortName)
    throws IOException {
    // Configuration.
    Map<String, String> properties = setPutDatabaseRecordProperties(statementType, tableName);
    properties.put(PUT_DB_RECORD_READER, recordReaderId);
    properties.put(PUT_DB_RECORD_DCBP_SERVICE, dcbpServiceId);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
      Arrays.asList(RELATIONSHIP_TYPE.SUCCESS.getType())));
    processorConfigDTO.setProperties(properties);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_DATABASE_RECORD,
      name, processorConfigDTO, bundleDTO);
    Set<String> relationshipTypes = new HashSet<>(Arrays.asList(
      RELATIONSHIP_TYPE.FAILURE.getType(),
      RELATIONSHIP_TYPE.RETRY.getType()
    ));

    ProcessorEntity processor = createProcessor(parentProcessGroupId, processorDTO,
      relationshipTypes, outputPortName);
    createUpstreamConnection(processor, parentProcessGroupId, inputPortName);

    return processor;
  }

  public ProcessorEntity updatePutDatabaseRecord(@NotNull String processorId,
    NifiConstants.Properties.Values.STATEMENT_TYPE statementType,
    String tableName)
    throws IOException {
    return updateProcessor(processorId, setPutDatabaseRecordProperties(statementType, tableName));
  }

  private Map<String, String> setPutDatabaseRecordProperties(
    NifiConstants.Properties.Values.STATEMENT_TYPE statementType,
    @NotNull String tableName) {
    Map<String, String> properties = new HashMap<>();

    properties.put(PUT_DB_RECORD_STATEMENT_TYPE, statementType.getType());
    properties.put(PUT_DB_RECORD_TABLE_NAME, tableName);

    return properties;
  }


  /**
   * Helper method to create a processor.
   *
   * @param parentProcessGroupId The id of the parent group where the processor will be created.
   * @param processorDTO A ProcessorDTO containing all needed values for the creation.
   * @param relationshipTypes The relationship types of the connection between the processor and the
   * output port.
   * @param outputPortName The name of the output port that the processor will be connected to.
   * @return A ProcessorEntity object containing the newly created processor.
   */
  private ProcessorEntity createProcessor(@NotNull String parentProcessGroupId,
    ProcessorDTO processorDTO, Set<String> relationshipTypes, String outputPortName)
    throws IOException {

    RevisionDTO revisionDTO = createRevisionDTO();

    ProcessorEntity processorEntity = new ProcessorEntity();
    processorEntity.setComponent(processorDTO);
    processorEntity.setRevision(revisionDTO);

    // Create processor.
    final CallReplyDTO callReplyDTO = postJSONCall(
      "/process-groups/" + parentProcessGroupId + "/processors", processorEntity);

    // Return if processor creation was unsuccessfull.
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

    // Find the output port available on this processor's process group.
    final PortEntity portEntity = findOutputPortByName(parentProcessGroupId, outputPortName);
    if (portEntity != null) {

      // Connect the processor to the output ports.
      processorEntity = mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);

      ConnectionEntity connectionEntity = new ConnectionEntity();
      ConnectionDTO connectionDTO = new ConnectionDTO();

      ConnectableDTO sourceDTO = new ConnectableDTO();
      sourceDTO.setType(String.valueOf(PROCESSOR));
      sourceDTO.setId(processorEntity.getId());
      sourceDTO.setGroupId(parentProcessGroupId);

      ConnectableDTO destinationDTO = new ConnectableDTO();
      destinationDTO.setGroupId(parentProcessGroupId);
      destinationDTO.setType(String.valueOf(OUTPUT_PORT));
      destinationDTO.setId(portEntity.getId());

      connectionDTO.setSource(sourceDTO);
      connectionDTO.setDestination(destinationDTO);
      connectionDTO.setSelectedRelationships(relationshipTypes);
      connectionEntity.setComponent(connectionDTO);
      connectionEntity.setRevision(createRevisionDTO());

      postJSONCall("/process-groups/" + parentProcessGroupId + "/connections", connectionEntity);
      if (!callReplyDTO.isSuccessful()) {
        throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
      }
    }

    //todo update processorEntity object to include the newly created connection.
    return processorEntity;
  }

  private ProcessorEntity updateProcessor(String processorId, Map<String, String> properties)
    throws IOException {
    ProcessorEntity latestEntity = getProcessorById(processorId);
    Long currentVersion = latestEntity.getRevision().getVersion();

    ProcessorDTO processorDTO = new ProcessorDTO();
    processorDTO.setId(processorId);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);

    processorDTO.setConfig(processorConfigDTO);

    RevisionDTO revisionDTO = createRevisionDTO();
    revisionDTO.setVersion(currentVersion);
    revisionDTO.setClientId(processorId);

    ProcessorEntity processorEntity = new ProcessorEntity();
    processorEntity.setRevision(revisionDTO);
    processorEntity.setComponent(processorDTO);

    CallReplyDTO callReplyDTO = putJSONCall("/processors/" + processorId, processorEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Changes the status of a Processor..
   *
   * @param processorId The id of the Processor.
   * @param state The desired state of the component.
   * @return ProcessorEntity object containing the updated processor.
   */
  public ProcessorEntity changeProcessorStatus(String processorId, STATE state) throws IOException {
    ProcessorEntity processorEntity = getProcessorById(processorId);

    ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
    processorRunStatusEntity.setState(state.name());
    processorRunStatusEntity.setRevision(processorEntity.getRevision());

    final CallReplyDTO callReplyDTO = putJSONCall("/processors/" + processorId + "/run-status/",
      processorRunStatusEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Deletes a processor. If the processor is the destination of a connection, the connection will
   * be also deleted.
   *
   * @param processorId The id of the processor to delete.
   * @return ProcessorEntity object containing the deleted processor.
   */
  public ProcessorEntity deleteProcessor(String processorId) throws IOException {
    ProcessorEntity processorEntity = this.changeProcessorStatus(processorId,
      STATE.STOPPED);

    ProcessGroupFlowEntity processGroups = getProcessGroups(
      processorEntity.getComponent().getParentGroupId());

    List<ConnectionEntity> connections = processGroups.getProcessGroupFlow().getFlow()
      .getConnections().stream()
      .filter(connectionEntity -> connectionEntity.getDestinationId().equals(processorId))
      .collect(
        Collectors.toList());

    for (ConnectionEntity connectionEntity : connections) {
      CallReplyDTO callReplyDTO = deleteConnection(connectionEntity);
      if (!callReplyDTO.isSuccessful()) {
        throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
      }
    }

    CallReplyDTO callReplyDTO =
      deleteCall("/processors/" + processorId
        + "?version=" + processorEntity.getRevision().getVersion()
        + "&clientId=" + processorId
        + "&disconnectedNodeAcknowledged=false");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public Collection<String> getValidationErrors(String processorId) throws IOException {
    return getProcessorById(processorId).getComponent().getValidationErrors();
  }

  private ProcessorEntity getProcessorById(String processorId) throws IOException {
    CallReplyDTO callReplyDTO = getCall("/processors/" + processorId);
    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(),
        callReplyDTO.getCode());
    }
  }

  /**
   * Deletes a connection.
   *
   * @param connectionEntity the connection to delete.
   * @return The result of the delete REST call.
   */
  private CallReplyDTO deleteConnection(ConnectionEntity connectionEntity) throws IOException {

    return deleteCall("/connections/" + connectionEntity.getId() + "?version"
      + "=" + connectionEntity.getRevision().getVersion());
  }

  private RevisionDTO createRevisionDTO() {
    RevisionDTO revisionDTO = new RevisionDTO();
    revisionDTO.setVersion(0l);
    return revisionDTO;
  }

  private BundleDTO createBundleDTO(String bundleGroup, String bundleArtifact) {
    BundleDTO bundleDTO = new BundleDTO();
    bundleDTO.setGroup(bundleGroup);
    bundleDTO.setArtifact(bundleArtifact);
    bundleDTO.setVersion(NIFI_VERSION);
    return bundleDTO;
  }

  private ControllerServiceDTO createControllerServiceDTO(String controllerServiceType,
    String controllerServiceName, BundleDTO bundleDTO) {
    ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
    controllerServiceDTO.setType(controllerServiceType);
    controllerServiceDTO.setName(controllerServiceName);
    controllerServiceDTO.setBundle(bundleDTO);
    return controllerServiceDTO;
  }

  private ProcessorDTO createProcessorDTO(String processorType, String processorName,
    ProcessorConfigDTO processorConfigDTO, BundleDTO bundleDTO) {
    ProcessorDTO processorDTO = new ProcessorDTO();
    processorDTO.setType(processorType);
    processorDTO.setName(processorName);
    processorDTO.setBundle(bundleDTO);
    processorDTO.setConfig(processorConfigDTO);
    return processorDTO;
  }

}
