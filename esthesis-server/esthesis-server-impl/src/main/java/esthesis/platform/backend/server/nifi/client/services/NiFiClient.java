package esthesis.platform.backend.server.nifi.client.services;

import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.Type.DISTRIBUTE_LOAD;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.CLIENT_ID;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.CONTROLLER_SERVICES;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.FLOW;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.PROCESSORS;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.PROCESS_GROUPS;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.RUN_STATUS;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.UrlPaths.VERSION;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.COMMAND_REQUEST_INSERT_QUERY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.CONFLICT_RESOLUTION_STRATEGY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DB_CONNECTION_URL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DB_DRIVER_CLASS_NAME;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DB_DRIVER_LOCATION;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DB_USER;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DCBP_SERVICE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DEVICE_ID_QUERY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.DIRECTORY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.ESTHESIS_HARDWARE_ID;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.HOSTNAME;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_CHARSET;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_CONSISTENCY_LEVEL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_DB_NAME;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_MAX_CONNECTION_TIMEOUT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_MAX_RECORDS_SIZE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_PASSWORD;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_QUERY_CHUNK_SIZE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_QUERY_RESULT_TIME_UNIT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_RETENTION_POLICY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_URL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.INFLUX_USERNAME;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.JDBC_CONNECTION_POOL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.MESSAGE_BODY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.MESSAGE_PRIORITY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.OBTAIN_GENERATED_KEYS;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PORT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PROTOCOL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PSWD;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_DB_RECORD_DCBP_SERVICE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_DB_RECORD_FIELD_CONTAINING_SQL;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_DB_RECORD_READER;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_DB_RECORD_STATEMENT_TYPE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_DB_RECORD_TABLE_NAME;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.PUT_SQL_STATEMENT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.SQL_SELECT_QUERY;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.SSL_CONTEXT_SERVICE;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.INPUT_PORT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.OUTPUT_PORT;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.CONNECTABLE_COMPONENT_TYPE.PROCESSOR;
import static esthesis.platform.backend.server.nifi.client.util.NiFiConstants.SyncErrors.NON_EXISTENT_PROCESSOR;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import esthesis.platform.backend.server.nifi.client.dto.CallReplyDTO;
import esthesis.platform.backend.server.nifi.client.dto.NiFiSearchAlgorithm;
import esthesis.platform.backend.server.nifi.client.exception.NiFiProcessingException;
import esthesis.platform.backend.server.nifi.client.util.JacksonIgnoreInvalidFormatHandler;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Bundle.BundleArtifact;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Bundle.BundleGroup;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.ControllerService.Type;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Processor.ExistingProcessorSuffix;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.CONSISTENCY_LEVEL;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.DATA_UNIT;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.FAILED_RELATIONSHIP_TYPES;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.KEY_TRUST_STORE_TYPE;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATEMENT_TYPE;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.SUCCESSFUL_RELATIONSHIP_TYPES;
import esthesis.platform.backend.server.nifi.client.util.Util;
import esthesis.platform.backend.server.service.NiFiService;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BundleDTO;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.PositionDTO;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.RelationshipDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.AboutEntity;
import org.apache.nifi.web.api.entity.ActivateControllerServicesEntity;
import org.apache.nifi.web.api.entity.BulletinEntity;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServiceRunStatusEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.ControllerStatusEntity;
import org.apache.nifi.web.api.entity.FlowEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.InstantiateTemplateRequestEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.apache.nifi.web.api.entity.PortRunStatusEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupFlowEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.ProcessorRunStatusEntity;
import org.apache.nifi.web.api.entity.ProcessorsEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Log
@Component
public class NiFiClient {

  //TODO: Both variables next, will be fetched by the NiFiConnector services of esthesis
  // (NiFiConnector should cache these values whenever a NiFi server is registered/edited).
  private static final String NIFI_VERSION = "1.12.1";

  private static final ObjectMapper mapper = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .addHandler(new JacksonIgnoreInvalidFormatHandler());
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
        .addHandler(new JacksonIgnoreInvalidFormatHandler());
  }

  private String getNiFiUrl() {
    return niFiService.getActiveNiFi().getUrl() + "/nifi-api";
  }

  private CallReplyDTO prepareReply(Response response) throws IOException {
    CallReplyDTO callReplyDTO = new CallReplyDTO();
    callReplyDTO.setCode(response.code());
    callReplyDTO.setSuccessful(response.isSuccessful());
    if (response.body() != null) {
      callReplyDTO.setBody(response.body().string());
    }

    return callReplyDTO;
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

  private String getNifiGroupByIdUrlPath(String processGroupId) {
    return "/" + PROCESS_GROUPS + "/" + processGroupId;
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
    if (httpMethod.equals(HTTP_METHOD.POST)) {
      requestBuilder.post(body);
    } else if (httpMethod.equals(HTTP_METHOD.PUT)) {
      requestBuilder.put(body);
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
    final CallReplyDTO callReplyDTO = getCall("/" + FLOW + "/about");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), AboutEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public ControllerStatusEntity getStatus() throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/" + FLOW + "/status");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ControllerStatusEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Uploads a template.
   *
   * @param templateResource The classpath resource to upload.
   */
  public TemplateEntity uploadTemplate(String templateResource) throws IOException {
    String rootProcessGroupId = getRootProcessGroup().getProcessGroupFlow().getId();
    String templateBody = Util.readFromClasspath(templateResource);

    final CallReplyDTO callReplyDTO = postFormCall(
        getNifiGroupByIdUrlPath(rootProcessGroupId) + "/templates/upload",
        ImmutableMap.of("template", templateBody));

    if (callReplyDTO.isSuccessful()) {
      return xmlMapper.readValue(callReplyDTO.getBody(), TemplateEntity.class);
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

    final CallReplyDTO callReplyDTO = postJSONCall(getNifiGroupByIdUrlPath(rootProcessGroupId) +
        "/template-instance", request);

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
    final CallReplyDTO callReplyDTO = getCall("/" + FLOW + "/templates");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), TemplatesEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public void deleteTemplate(String templateId) throws IOException {
    deleteCall("/templates/" + templateId);
  }

  public void deleteProcessGroup(String processGroupId) throws IOException {
    ProcessGroupEntity processGroup = getProcessGroup(processGroupId);

    CallReplyDTO callReplyDTO =
        deleteCall(getNifiGroupByIdUrlPath(processGroupId)
            + VERSION + processGroup.getRevision().getVersion()
            + CLIENT_ID + processGroup.getRevision().getClientId());

    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

  }

  private ProcessGroupEntity getProcessGroup(String processGroupId) throws IOException {
    final CallReplyDTO callReplyDTO = getCall(getNifiGroupByIdUrlPath(processGroupId));
    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessGroupEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Returns the Id of the root process group under which everything else resides.
   */
  public ProcessGroupFlowEntity getRootProcessGroup() throws IOException {
    final CallReplyDTO callReplyDTO = getCall("/" + FLOW + "/" + PROCESS_GROUPS + "/" + "root");
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
    final CallReplyDTO callReplyDTO =
        getCall("/" + FLOW + getNifiGroupByIdUrlPath(parentProcessGroupId));

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessGroupFlowEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public ProcessorsEntity getProcessorsOfGroup(String parentProcessGroupId) throws IOException {
    final CallReplyDTO callReplyDTO = getCall(
        getNifiGroupByIdUrlPath(parentProcessGroupId) + "/" + PROCESSORS);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(),
          ProcessorsEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public void changeGroupControllerServicesState(String parentGroupId,
      STATE state) throws IOException {
    ActivateControllerServicesEntity activateControllerServicesEntity =
        new ActivateControllerServicesEntity();

    activateControllerServicesEntity.setId(parentGroupId);
    activateControllerServicesEntity.setState(state.name());

    final CallReplyDTO callReplyDTO = putJSONCall(
        "/" + FLOW + getNifiGroupByIdUrlPath(parentGroupId) + "/" + CONTROLLER_SERVICES,
        activateControllerServicesEntity);

    if (!callReplyDTO.isSuccessful()) {
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
        processGroupEntity = match;
      } else {
        break;
      }
    }

    return processGroupEntity;
  }

  public Optional<ProcessorEntity> findProcessorInGroup(NiFiSearchAlgorithm niFiSearchAlgorithm,
      List<String> searchPath, String nameSearchTerm) throws IOException {
    Optional<ProcessGroupEntity> processGroup = findProcessGroup(NiFiSearchAlgorithm.NAME_ENDS_WITH,
        searchPath);
    Optional<ProcessorEntity> processorEntity = Optional.empty();

    if (processGroup.isPresent()) {
      ProcessorsEntity processorsEntity = getProcessorsOfGroup(processGroup.get().getId());
      processorEntity = processorsEntity.getProcessors().stream().filter(p -> {
        switch (niFiSearchAlgorithm) {
          case NAME_ENDS_WITH:
            return p.getComponent().getName().endsWith(nameSearchTerm);
          case NAME_EQUALS:
            return p.getComponent().getName().equals(nameSearchTerm);
          default:
            return false;
        }
      }).findFirst();
    }

    return processorEntity;
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
        "/" + FLOW + getNifiGroupByIdUrlPath(parentProcessGroupId) + "/" + CONTROLLER_SERVICES);

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

  public boolean controllerExists(String controllerServiceId)
      throws IOException {
    CallReplyDTO callReplyDTO = getCall("/" + CONTROLLER_SERVICES + "/" + controllerServiceId);

    return callReplyDTO.isSuccessful();
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

    final CallReplyDTO callReplyDTO = putJSONCall(
        "/" + FLOW + getNifiGroupByIdUrlPath(processGroupId),
        scheduleComponentsEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ScheduleComponentsEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Searches for an Input Port by parent group id.
   *
   * @param parentProcessGroupId The id of the group where the input port is located.
   * @return PortEntity object containing the wanted input port.
   */
  public PortEntity findInputPort(String parentProcessGroupId)
      throws IOException {
    final CallReplyDTO callReplyDTO = getCall(
        getNifiGroupByIdUrlPath(parentProcessGroupId) + "/input-ports");

    if (callReplyDTO.isSuccessful()) {
      InputPortsEntity inputPortsEntity = mapper
          .readValue(callReplyDTO.getBody(), InputPortsEntity.class);
      Optional<PortEntity> optionalPortEntity = inputPortsEntity.getInputPorts().stream()
          .findFirst();
      return optionalPortEntity.orElse(null);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Searches for the output ports by parent group id.
   *
   * @param parentProcessGroupId The id of the group where the output port is located.
   * @return PortEntity object containing the wanted output port.
   */
  public OutputPortsEntity findOutputPorts(String parentProcessGroupId)
      throws IOException {
    final CallReplyDTO callReplyDTO = getCall(getNifiGroupByIdUrlPath(parentProcessGroupId) +
        "/output-ports");

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), OutputPortsEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  private void changePortStatus(PortEntity portEntity, STATE state) throws IOException {
    PortRunStatusEntity portRunStatusEntity = new PortRunStatusEntity();
    portRunStatusEntity.setState(state.name());
    portRunStatusEntity.setRevision(portEntity.getRevision());

    String portTypePath = portEntity.getPortType().equals("INPUT_PORT") ? "input-ports/" : "output"
        + "-ports/";

    final CallReplyDTO callReplyDTO = putJSONCall("/" + portTypePath + portEntity.getId() + "/run"
        + "-status", portRunStatusEntity);

    if (!callReplyDTO.isSuccessful()) {
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
    properties.put(Properties.KEYSTORE_TYPE, KEY_TRUST_STORE_TYPE.PKCS12.getType());
    properties.put(Properties.TRUSTSTORE_FILENAME, truststoreFilename);
    properties.put(Properties.TRUSTSTORE_PSWD, truststorePassword);
    properties.put(Properties.TRUSTSTORE_TYPE, KEY_TRUST_STORE_TYPE.PKCS12.getType());
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
    properties.put(Properties.KEYSTORE_TYPE, KEY_TRUST_STORE_TYPE.PKCS12.getType());
    properties.put(Properties.TRUSTSTORE_FILENAME, truststoreFilename);
    properties.put(Properties.TRUSTSTORE_PSWD, truststorePassword);
    properties.put(Properties.TRUSTSTORE_TYPE, KEY_TRUST_STORE_TYPE.PKCS12.getType());
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
    properties.put(DB_CONNECTION_URL, databaseConnectionURL);
    properties.put(DB_DRIVER_CLASS_NAME, databaseDriverClassName);
    properties.put(DB_DRIVER_LOCATION, databaseDriverClassLocation);
    properties.put(DB_USER, databaseUser);
    properties.put(PSWD, password);

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
    properties.put(DB_CONNECTION_URL, databaseConnectionURL);
    properties.put(DB_DRIVER_CLASS_NAME, databaseDriverClassName);
    properties.put(DB_DRIVER_LOCATION, databaseDriverClassLocation);
    properties.put(DB_USER, databaseUser);
    properties.put(PSWD, password);

    return updateController(id, properties);
  }

  /**
   * Creates a JSONTreeReader processor.
   *
   * @param name The name of the processor.
   * @param parentProcessGroupId The group where the processor will be created.
   * @return The ControlServiceEntity containing the created JSON Tree Reader service.
   */
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
    CallReplyDTO callReplyDTO = postJSONCall(getNifiGroupByIdUrlPath(parentProcessGroupId) +
            "/" + CONTROLLER_SERVICES,
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
    CallReplyDTO callReplyDTO = getCall("/" + CONTROLLER_SERVICES + "/" + id);

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

    callReplyDTO = putJSONCall("/" + CONTROLLER_SERVICES + "/" + id, updatedEntity);

    if (callReplyDTO.isSuccessful()) {
      changeControllerServiceStatus(id, STATE.ENABLED);
      return mapper.readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  private String findControllerStatus(String controllerId) throws IOException {
    CallReplyDTO callReplyDTO = getCall("/" + CONTROLLER_SERVICES + "/" + controllerId);
    if (callReplyDTO.isSuccessful()) {
      ControllerServiceEntity controllerServiceEntity = mapper
          .readValue(callReplyDTO.getBody(), ControllerServiceEntity.class);
      return controllerServiceEntity.getStatus().getRunStatus();
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

    await().atMost(Duration.ofSeconds(30))
        .until(() -> findControllerStatus(id).equals(STATE.DISABLED.name()));

    CallReplyDTO callReplyDTO =
        deleteCall(
            "/" + CONTROLLER_SERVICES + "/" + id + VERSION + controllerServiceEntity.getRevision()
                .getVersion() +
                CLIENT_ID + id
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

    CallReplyDTO getControllerCallReplyDTO =
        getCall("/" + CONTROLLER_SERVICES + "/" + controllerServiceId);

    if (getControllerCallReplyDTO.isSuccessful()) {
      ControllerServiceEntity controllerServiceEntity = mapper
          .readValue(getControllerCallReplyDTO.getBody(), ControllerServiceEntity.class);
      ControllerServiceRunStatusEntity controllerServiceRunStatusEntity =
          new ControllerServiceRunStatusEntity();

      controllerServiceRunStatusEntity.setRevision(controllerServiceEntity.getRevision());
      controllerServiceRunStatusEntity.setState(state.name());

      CallReplyDTO callReplyDTO =
          putJSONCall("/" + CONTROLLER_SERVICES + "/" + controllerServiceId + "/run"
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
   * Finds a DistributeLoad processor in the given parent group.
   *
   * @param parentProcessGroupId The id of the parent group.
   * @return ProcessorEnity object containing the newly created processor.
   */
  private ProcessorEntity findDistributeLoad(String parentProcessGroupId) throws IOException {
    final CallReplyDTO callReplyDTO = getCall(
        getNifiGroupByIdUrlPath(parentProcessGroupId) + "/" + PROCESSORS);

    if (callReplyDTO.isSuccessful()) {
      ProcessorsEntity processorsEntity = mapper
          .readValue(callReplyDTO.getBody(), ProcessorsEntity.class);
      Optional<ProcessorEntity> optionalDistributeLoad = processorsEntity.getProcessors().stream()
          .filter(
              processorEntity -> processorEntity.getComponent().getType().equals(DISTRIBUTE_LOAD))
          .findFirst();

      return optionalDistributeLoad.orElse(null);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

  }

  public ProcessorEntity createMQTTPublisher(@NotNull String parentProcessGroupId,
      @NotNull String name,
      @NotNull String uri, @NotNull String topic, int qos, boolean retainMesage,
      @Nullable String sslContextServiceId, String schedulingPeriod,
      boolean isCommandHandler)
      throws IOException {

    // Configuration.
    Map<String, String> properties = setPublishMQTTProperties(uri, sslContextServiceId, topic,
        qos, retainMesage);
    properties.put(Properties.CLIENT_ID, name + " [NIFI] ");

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.MQTT);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.MQTT_PUBLISH,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO, isCommandHandler);
  }

  public ProcessorEntity updateMQTTPublisher(String id, String name, String sslContextId,
      String uri, String topic, int qos, boolean retainMessage, String schedulingPeriod)
      throws IOException {

    Map<String, String> properties = setPublishMQTTProperties(uri, sslContextId, topic,
        qos, retainMessage);

    return updateProcessor(id, name, schedulingPeriod, properties);
  }

  private Map<String, String> setPublishMQTTProperties(String uri, String sslContextServiceId,
      @NotNull String topic,
      int qos,
      boolean retainMessage) {
    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.BROKER_URI, uri);
    properties.put(Properties.RETAIN_MSG, String.valueOf(retainMessage));
    properties.put(Properties.TOPIC, topic + "/" + ESTHESIS_HARDWARE_ID);
    properties.put(Properties.QOS, String.valueOf(qos));
    if (StringUtils.isNotBlank(sslContextServiceId)) {
      properties.put(SSL_CONTEXT_SERVICE, sslContextServiceId);
    }

    return properties;
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
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createMQTTConsumer(@NotNull String parentProcessGroupId,
      @NotNull String name,
      @NotNull String uri, @NotNull String topic, int qos, int queueSize,
      @Nullable String sslContextServiceId, String schedulingPeriod,
      boolean isCommandHandler)
      throws IOException {

    // Configuration.
    Map<String, String> properties = setConsumeMQTTProperties(uri, sslContextServiceId, topic,
        qos, queueSize);
    properties.put(Properties.CLIENT_ID, name + " [NIFI] ");

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.MQTT);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.MQTT_CONSUME,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO, isCommandHandler);
  }

  public ProcessorEntity updateConsumeMQTT(String id, String name, String sslContextId,
      @NotNull String uri,
      @NotNull String topic,
      int qos,
      int queueSize, String schedulingPeriod) throws IOException {
    Map<String, String> properties = setConsumeMQTTProperties(uri, sslContextId, topic, qos,
        queueSize);
    return updateProcessor(id, name, schedulingPeriod, properties);
  }

  private Map<String, String> setConsumeMQTTProperties(String uri, String sslContextServiceId,
      @NotNull String topic,
      int qos,
      int queueSize) {
    Map<String, String> properties = new HashMap<>();
    properties.put(Properties.BROKER_URI, uri);
    properties.put(Properties.MAX_QUEUE_SIZE, String.valueOf(queueSize));
    properties.put(Properties.TOPIC_FILTER, topic);
    properties.put(Properties.QOS, String.valueOf(qos));
    if (StringUtils.isNotBlank(sslContextServiceId)) {
      properties.put(SSL_CONTEXT_SERVICE, sslContextServiceId);
    }

    return properties;
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
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createPutInfluxDB(@NotNull String parentProcessGroupId,
      @NotNull String name, @NotNull String dbName, @NotNull String url,
      int maxConnectionTimeoutSeconds, String username, String password, String charset,
      CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
      DATA_UNIT maxRecordSizeUnit, String schedulingPeriod) throws IOException {

    // Configuration.
    Map<String, String> properties = setPutInfluxDBProperties(dbName, url,
        maxConnectionTimeoutSeconds, username, password, charset, level, retentionPolicy,
        maxRecordSize, maxRecordSizeUnit);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
        Collections.singletonList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType())));
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.INFLUX_DB);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_INFLUX_DB,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO);
  }

  /**
   * Updates a PutInfluxDB processor.
   *
   * @param processorId The id of the processor that will be updated.
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
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity updatePutInfluxDB(String processorId, String name,
      @NotNull String dbName,
      @NotNull String url,
      int maxConnectionTimeoutSeconds, String username, String password, String charset,
      CONSISTENCY_LEVEL level, String retentionPolicy, int maxRecordSize,
      DATA_UNIT maxRecordSizeUnit, String schedulingPeriod) throws IOException {

    // Configuration.
    Map<String, String> properties = setPutInfluxDBProperties(dbName, url,
        maxConnectionTimeoutSeconds, username, password, charset, level, retentionPolicy,
        maxRecordSize, maxRecordSizeUnit);

    return updateProcessor(processorId, name, schedulingPeriod, properties);

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
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity createPutDatabaseRecord(@NotNull String parentProcessGroupId,
      @NotNull String name, @NotNull String recordReaderId, @NotNull String dcbpServiceId,
      @NotNull NiFiConstants.Properties.Values.STATEMENT_TYPE statementType,
      String schedulingPeriod, boolean isCommandHandler)
      throws IOException {
    // Configuration.
    Map<String, String> properties = setPutDatabaseRecordProperties(statementType);
    properties.put(PUT_DB_RECORD_READER, recordReaderId);
    properties.put(PUT_DB_RECORD_DCBP_SERVICE, dcbpServiceId);
    properties.put(PUT_DB_RECORD_TABLE_NAME, isCommandHandler ? "command_reply" : "${esthesis"
        + ".measurement}");

    if (statementType.equals(STATEMENT_TYPE.USE_STATE_ATTRIBUTE)) {
      properties.put(PUT_DB_RECORD_FIELD_CONTAINING_SQL, "sql");
    }

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    processorConfigDTO.setAutoTerminatedRelationships(
        Collections.singleton(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType()));
    processorConfigDTO.setProperties(properties);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_DATABASE_RECORD,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO, isCommandHandler);
  }

  /**
   * Creates a PutDatabaseRecord processor.
   *
   * @param processorId The id of the PutDatabaseProcessor that will be updated.
   * @param name The name of the processor.
   * @param statementType Specifies the type of SQL Statement to generate.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return ProcessorEntity object containing the newly created processor.
   */
  public ProcessorEntity updatePutDatabaseRecord(@NotNull String processorId, String name,
      NiFiConstants.Properties.Values.STATEMENT_TYPE statementType, String schedulingPeriod)
      throws IOException {
    return updateProcessor(processorId, name, schedulingPeriod,
        setPutDatabaseRecordProperties(statementType));
  }

  private Map<String, String> setPutDatabaseRecordProperties(
      NiFiConstants.Properties.Values.STATEMENT_TYPE statementType) {
    Map<String, String> properties = new HashMap<>();

    properties.put(PUT_DB_RECORD_STATEMENT_TYPE, statementType.getType());

    return properties;
  }

  /**
   * Creates an ExecuteInfluxDb processor.
   *
   * @param parentProcessGroupId The id of the group that the processor will be created.
   * @param name The name of the processor.
   * @param dbName The name of the Influx Database.
   * @param url The url of the Influx Database.
   * @param maxConnectionTimeoutSeconds The maximum time of establising connection to Influx
   * Database
   * @param queryResultTimeUnit The time unit of query results from theInflux Database.
   * @param queryChunkSize The chunk size of the query result.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The newly created processor.
   */
  public ProcessorEntity createExecuteInfluxDB(@NotNull String parentProcessGroupId,
      @NotNull String name, @NotNull String dbName, @NotNull String url,
      int maxConnectionTimeoutSeconds, String username, String password, String queryResultTimeUnit,
      int queryChunkSize, String schedulingPeriod) throws IOException {

    Map<String, String> properties = setExecuteInfluxDBProperties(dbName, url,
        maxConnectionTimeoutSeconds, username, password, queryResultTimeUnit, queryChunkSize);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.INFLUX_DB);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.EXECUTE_INFLUX_DB,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO);
  }

  /**
   * Updates an ExecuteInfluxDb processor.
   *
   * @param processorId The id of the processor.
   * @param name The name of the processor.
   * @param dbName The name of the Influx Database.
   * @param url The url of the Influx Database.
   * @param maxConnectionTimeoutSeconds The maximum time of establising connection to Influx
   * Database
   * @param queryResultTimeUnit The time unit of query results from theInflux Database.
   * @param queryChunkSize The chunk size of the query result.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The updated processor.
   */
  public ProcessorEntity updateExecuteInfluxDB(String processorId, String name, String dbName,
      String url,
      int maxConnectionTimeoutSeconds, String username, String password, String queryResultTimeUnit
      , int queryChunkSize,
      String schedulingPeriod)
      throws IOException {

    Map<String, String> properties = setExecuteInfluxDBProperties(dbName, url,
        maxConnectionTimeoutSeconds, username, password, queryResultTimeUnit, queryChunkSize);

    return updateProcessor(processorId, name, schedulingPeriod, properties);

  }

  private Map<String, String> setExecuteInfluxDBProperties(@NotNull String dbName,
      @NotNull String url,
      int maxConnectionTimeoutSeconds, String username, String password, String queryResultTimeUnit,
      int queryChunkSize) {

    Map<String, String> properties = new HashMap<>();
    properties.put(INFLUX_DB_NAME, dbName);
    properties.put(INFLUX_URL, url);
    properties.put(INFLUX_USERNAME, username);
    properties.put(INFLUX_PASSWORD, password);
    properties.put(INFLUX_MAX_CONNECTION_TIMEOUT, (maxConnectionTimeoutSeconds) +
        " seconds");
    properties.put(INFLUX_QUERY_RESULT_TIME_UNIT, queryResultTimeUnit.toUpperCase());
    properties.put(INFLUX_QUERY_CHUNK_SIZE, String.valueOf(queryChunkSize));

    return properties;
  }

  /**
   * Creates an ExecuteSQL processor.
   *
   * @param parentProcessGroupId The if od the parent group where the processor will be created.
   * @param name The name of the processor.
   * @param dcbpServiceId The if of the Database Connection Pool service.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The newly created processor.
   */
  public ProcessorEntity createExecuteSQL(@NotNull String parentProcessGroupId,
      @NotNull String name, @NotNull String dcbpServiceId, String schedulingPeriod,
      boolean isCommandHandler)
      throws IOException {

    Map<String, String> properties = new HashMap<>();
    properties.put(DCBP_SERVICE, dcbpServiceId);

    if (isCommandHandler) {
      properties.put(SQL_SELECT_QUERY, DEVICE_ID_QUERY);
    }

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.EXECUTE_SQL,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO, isCommandHandler);
  }

  public ProcessorEntity updateExecuteSQL(String processorId, String name, String schedulingPeriod)
      throws IOException {
    return updateProcessor(processorId, name, schedulingPeriod, new HashMap<>());
  }

  /**
   * Creates a PutFile processor.
   *
   * @param parentProcessGroupId The if od the parent group where the processor will be created.
   * @param name The name of the processor.
   * @param directory The directory where the files will be created.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The newly created processor.
   */
  public ProcessorEntity createPutFile(String parentProcessGroupId, String name, String directory,
      String schedulingPeriod)
      throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put(DIRECTORY, directory);
    properties.put(CONFLICT_RESOLUTION_STRATEGY, "replace");

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
        Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType(),
            FAILED_RELATIONSHIP_TYPES.FAILURE.getType())));
    processorConfigDTO.setProperties(properties);
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_FILE,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO);
  }

  /**
   * Updates a PutFile processor.
   *
   * @param processorId The id of the processor to update.
   * @param name The name of the processor.
   * @param directory he directory where the files will be created.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The newly created processor.
   */
  public ProcessorEntity updatePutFile(String processorId, String name, String directory,
      String schedulingPeriod) throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put(DIRECTORY, directory);

    return updateProcessor(processorId, name, schedulingPeriod, properties);
  }

  /**
   * Creates a PutSyslog processor.
   *
   * @param name The name of the processor.
   * @param sslContextId The id of the SSL Context Service used to provide client certificate
   * information for TLS/SSL connections.
   * @param hostname The hostname of the Syslog server.
   * @param port The port of the Syslog server.
   * @param protocol The protocol used to communicate with the Syslog server (UDP / TCP).
   * @param messageBody The body of the Syslog message.
   * @param messagePriority The priority of the Syslog message.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The newly created processor.
   */
  public ProcessorEntity createPutSyslog(String parentProcessGroupId, String name,
      String sslContextId, String hostname, int port, String protocol, String messageBody,
      String messagePriority, String schedulingPeriod)
      throws IOException {

    Map<String, String> properties = setPutSyslogProperties(sslContextId, hostname, port,
        protocol, messageBody, messagePriority);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
        Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType(),
            FAILED_RELATIONSHIP_TYPES.FAILURE.getType(),
            FAILED_RELATIONSHIP_TYPES.INVALID.getType())));
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_SYSLOG,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO);
  }

  /**
   * Updates a PutSyslog processor.
   *
   * @param processorId The id of the processor that will be updated.
   * @param name The name of the processor.
   * @param hostname The hostname of the Syslog server.
   * @param port The port of the Syslog server.
   * @param protocol The protocol used to communicate with the Syslog server (UDP / TCP).
   * @param messageBody The body of the Syslog message.
   * @param messagePriority The priority of the Syslog message.
   * @param schedulingPeriod The amount of time that should elapse between task executions.
   * @return The id of the updated processor.
   */
  public ProcessorEntity updatePutSyslog(String processorId, String name, String sslContextId,
      String hostname,
      int port, String protocol, String messageBody, String messagePriority,
      String schedulingPeriod)
      throws IOException {

    Map<String, String> properties = setPutSyslogProperties(sslContextId, hostname, port,
        protocol, messageBody, messagePriority);

    return updateProcessor(processorId, name, schedulingPeriod, properties);
  }

  private Map<String, String> setPutSyslogProperties(String sslContextId, String hostname, int port,
      String protocol, String messageBody, String messagePriority) {

    Map<String, String> properties = new HashMap<>();
    properties.put(HOSTNAME, hostname);
    properties.put(PORT, "" + port);
    properties.put(PROTOCOL, protocol.toUpperCase());
    properties.put(MESSAGE_BODY, messageBody);
    properties.put(MESSAGE_PRIORITY, messagePriority);

    if (sslContextId != null) {
      properties.put(SSL_CONTEXT_SERVICE, sslContextId);
    }

    return properties;
  }


  public ProcessorEntity createPutSQL(String parentProcessGroupId, String name,
      String dbConnectionPool,
      String schedulingPeriod, boolean isCommandHandler) throws IOException {

    Map<String, String> properties = new HashMap<>();
    properties.put(JDBC_CONNECTION_POOL, dbConnectionPool);
    properties.put(PUT_SQL_STATEMENT, COMMAND_REQUEST_INSERT_QUERY);
    properties.put(OBTAIN_GENERATED_KEYS, "true");

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setProperties(properties);
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);
    processorConfigDTO.setAutoTerminatedRelationships(new HashSet<>(
        Arrays.asList(SUCCESSFUL_RELATIONSHIP_TYPES.SUCCESS.getType(),
            FAILED_RELATIONSHIP_TYPES.FAILURE.getType(),
            FAILED_RELATIONSHIP_TYPES.INVALID.getType())));
    BundleDTO bundleDTO = createBundleDTO(BundleGroup.NIFI, BundleArtifact.STANDARD);

    // Create the processor component for the resource.
    ProcessorDTO processorDTO = createProcessorDTO(Processor.Type.PUT_SQL,
        name, processorConfigDTO, bundleDTO);

    return createProcessor(parentProcessGroupId, processorDTO, isCommandHandler);
  }

  public ProcessorEntity updatePutSQL(String processorId, String name, String schedulingPeriod)
      throws IOException {

    return updateProcessor(processorId, name, schedulingPeriod, new HashMap<>());
  }

  /**
   * Distributes the load in the producers group.
   *
   * @param isRelational Whether the target is relational or infux.
   * @param rootGroupId The metadata/telemetry producers group id.
   * @param influxGroupId The id of the influx group.
   * @param relationalGroupId The id of the relational group.
   * @param influxInstanceGroupId The id of the influx instances group.
   * @param relationalInstanceGroupId The id of the relational instances group.
   */
  public void distributeLoadOfProducers(boolean isRelational, String rootGroupId,
      String influxGroupId,
      String relationalGroupId, String influxInstanceGroupId, String relationalInstanceGroupId)
      throws IOException {

    ProcessorsEntity allProcessorsOfRootGroup = getProcessorsOfGroup(rootGroupId);
    PortEntity rootGroupInputPort = findInputPort(rootGroupId);
    PortEntity typeGroupInputPort = findInputPort(isRelational ? relationalGroupId : influxGroupId);

    ProcessorEntity topLevelLoadDistributor = allProcessorsOfRootGroup
        .getProcessors().stream().filter(
            processorEntity -> processorEntity.getComponent().getType()
                .equals(Processor.Type.DISTRIBUTE_LOAD))
        .findFirst().get();

    List<ConnectionEntity> allConnectionsOfTopLevelLoadDistributor =
        getAllConnectionsOfProcessor(
            topLevelLoadDistributor.getId(), rootGroupId);

    ProcessorsEntity influxProcessors = getProcessorsOfGroup(influxInstanceGroupId);
    ProcessorsEntity relationalProcessors = getProcessorsOfGroup(relationalInstanceGroupId);

    long influxCount = influxProcessors.getProcessors().stream().filter(
            processorEntity -> processorEntity.getComponent().getType()
                .equals(Processor.Type.EXECUTE_INFLUX_DB))
        .count();

    long relationalCount = relationalProcessors.getProcessors().stream().filter(
            processorEntity -> processorEntity.getComponent().getType()
                .equals(Processor.Type.EXECUTE_SQL))
        .count();

    long typeCount = isRelational ? relationalCount : influxCount;
    long allProducers = influxCount + relationalCount;

    Optional<ConnectionEntity> portToLoadDistributorConnectionOpt = allConnectionsOfTopLevelLoadDistributor.stream()
        .filter(
            connectionEntity -> connectionEntity.getDestinationId()
                .equals(topLevelLoadDistributor.getId())).findFirst();

    if (allProducers > 0 && portToLoadDistributorConnectionOpt.isEmpty()) {
      connectSourceAndDestination(rootGroupId, rootGroupId,
          INPUT_PORT.name(), PROCESSOR.name(), rootGroupInputPort.getId(),
          topLevelLoadDistributor.getId(),
          new HashSet<>());
    }

    List<ConnectionEntity> distributeLoadOutgoingConnections =
        allConnectionsOfTopLevelLoadDistributor.stream().filter(
                connectionEntity -> connectionEntity.getSourceId()
                    .equals(topLevelLoadDistributor.getId()))
            .collect(Collectors.toList());
    int connections = distributeLoadOutgoingConnections.size();

    Optional<ConnectionEntity> typeConnection = distributeLoadOutgoingConnections.stream()
        .filter(connectionEntity -> connectionEntity.getDestinationId()
            .equals(typeGroupInputPort.getId()))
        .findFirst();

    Optional<ConnectionEntity> otherConnection = distributeLoadOutgoingConnections.stream()
        .filter(connectionEntity -> !connectionEntity.getDestinationId()
            .equals(typeGroupInputPort.getId()))
        .findFirst();

    if (connections < 2 && !typeConnection.isPresent()) {
      connections += 1;
      updateDistributeLoad(connections, topLevelLoadDistributor.getId());
      createDistributeLoadConnection(connections, rootGroupId, isRelational ?
              relationalGroupId : influxGroupId, topLevelLoadDistributor.getId(),
          typeGroupInputPort.getId());
    }

    String instanceGroupId = isRelational ? relationalInstanceGroupId : influxInstanceGroupId;
    ProcessorEntity instanceLoadDistributor = findDistributeLoad(instanceGroupId);

    if (instanceLoadDistributor != null) {
      updateConnectionsInInstancesGroup(instanceGroupId, instanceLoadDistributor);
    }

    if (typeConnection.isPresent() && typeCount == 0) {
      ConnectableDTO source = typeConnection.get().getComponent().getSource();
      deleteConnectionBetweenPortAndProcessor(typeGroupInputPort, source.getId(),
          typeConnection.get());
      if (connections == 2 && typeConnection.get().getStatus().getName().equals("1")) {
        ConnectionEntity connectionEntity = otherConnection.get();
        connectionEntity.getComponent().setSelectedRelationships(Collections.singleton("1"));
        updateConnection(connectionEntity);
      }
      updateDistributeLoad(--connections, topLevelLoadDistributor.getId());
      changeProcessorStatus(source.getId(), STATE.RUNNING);
    }

    if (allProducers > 0) {

      if (!topLevelLoadDistributor.getStatus().getRunStatus()
          .equals(STATE.RUNNING.name())) {
        changeProcessorStatus(topLevelLoadDistributor.getId(), STATE.RUNNING);
      }

      if (!typeGroupInputPort.getComponent().getState().equals(STATE.RUNNING.name())) {
        changePortStatus(typeGroupInputPort, STATE.RUNNING);
      }
    }

    managerLoadBalanceAndHttpResponseConnections(rootGroupId, allProcessorsOfRootGroup,
        rootGroupInputPort, topLevelLoadDistributor,
        allProducers, portToLoadDistributorConnectionOpt);

  }

  private void managerLoadBalanceAndHttpResponseConnections(String rootGroupId,
      ProcessorsEntity allProcessorsOfRootGroup,
      PortEntity rootGroupInputPort, ProcessorEntity topLevelLoadDistributor, long allProducers,
      Optional<ConnectionEntity> portToLoadDistributorConnectionOpt) throws IOException {
    Optional<ProcessorEntity> optionalHttpResponseProcessor = allProcessorsOfRootGroup.getProcessors()
        .stream()
        .filter(processorEntity -> processorEntity.getComponent().getName().endsWith(
            ExistingProcessorSuffix.HTTP_RESPONSE_HANDLER))
        .findFirst();

    if (optionalHttpResponseProcessor.isPresent()) {
      if (allProducers < 1 && portToLoadDistributorConnectionOpt.isPresent()) {
        deleteConnectionWithLoadBalancer(rootGroupInputPort, topLevelLoadDistributor.getId(),
            portToLoadDistributorConnectionOpt.get());
      }

      manageQueueHandling(rootGroupId, rootGroupInputPort,
          optionalHttpResponseProcessor.get().getId(), allProducers < 1);
    }
  }

  /**
   * Creates or deletes the connection with processor responsible for the queue handling.
   *
   * @param rootGroupId The id of the group where the queue handling is needed.
   * @param inputPort The input port of the group.
   * @param queueHandlerId The id of the processor that handles the queue, when sink is missing.
   * @param shouldCreateConnection Whether the connection between the port and the Clear Queue
   * processor should be created (or deleted).
   */
  public void manageQueueHandling(String rootGroupId, PortEntity inputPort,
      String queueHandlerId, boolean shouldCreateConnection) throws IOException {
    if (shouldCreateConnection) {
      connectSourceAndDestination(rootGroupId, rootGroupId, INPUT_PORT.name(), PROCESSOR.name(),
          inputPort.getId(), queueHandlerId, new HashSet<>());
      changeProcessorStatus(queueHandlerId, STATE.RUNNING);
    } else {
      List<ConnectionEntity> allConnectionsOfProcessor = getAllConnectionsOfProcessor(
          queueHandlerId, rootGroupId);
      if (!allConnectionsOfProcessor.isEmpty()) {
        deleteConnectionBetweenPortAndProcessor(inputPort, queueHandlerId,
            allConnectionsOfProcessor.get(0));
      }
    }
    changePortStatus(findInputPort(rootGroupId), STATE.RUNNING);
  }

  private void deleteConnectionWithLoadBalancer(PortEntity rootGroupInputPort,
      String topLevelLoadDistributor, ConnectionEntity portToLoadDistributorConnection)
      throws IOException {
    deleteConnectionBetweenPortAndProcessor(rootGroupInputPort, topLevelLoadDistributor,
        portToLoadDistributorConnection);
  }

  private void updateConnectionsInInstancesGroup(String groupId, ProcessorEntity distributeLoad)
      throws IOException {
    List<ConnectionEntity> distributeLoadOutgoingConnections = getDistributeLoadOutgoingConnections(
        groupId, distributeLoad);

    int existingConnections = distributeLoadOutgoingConnections.size();
    int numberOfRelationships = Integer
        .parseInt(distributeLoad.getComponent().getConfig().getProperties()
            .get("Number of Relationships"));

    if (numberOfRelationships != existingConnections) {
      changeProcessorStatus(distributeLoad.getId(), STATE.STOPPED);
      updateDistributeLoad(existingConnections, distributeLoad.getId());

      List<String> expectedNames = new ArrayList<>();
      for (int i = 1; i <= existingConnections; i++) {
        expectedNames.add(String.valueOf(i));
      }

      List<String> existingNames = distributeLoadOutgoingConnections.stream()
          .map(connectionEntity -> connectionEntity.getStatus().getName()).collect(
              Collectors.toList());

      String newName = "";
      for (String n : expectedNames) {
        if (!existingNames.contains(n)) {
          newName = n;
          break;
        }
      }

      Optional<ConnectionEntity> entityToUpdate = distributeLoadOutgoingConnections.stream().filter(
          connectionEntity -> Integer.parseInt(connectionEntity.getStatus().getName())
              > existingConnections).findAny();

      if (entityToUpdate.isPresent()) {
        String destinationId = entityToUpdate.get().getDestinationId();
        String destinationState = getProcessorById(destinationId).getComponent().getState();

        //Stop destination if it's running
        if (destinationState.equals(STATE.RUNNING.name())) {
          changeProcessorStatus(destinationId, STATE.STOPPED);
        }

        entityToUpdate.get().getComponent()
            .setSelectedRelationships(Collections.singleton(newName));
        updateConnection(entityToUpdate.get());

        //If destination was running, we need to restart it.
        if (destinationState.equals(STATE.RUNNING.name())) {
          changeProcessorStatus(destinationId, STATE.RUNNING);
        }
        changeProcessorStatus(distributeLoad.getId(), STATE.RUNNING);
      }

    }
  }

  private void updateDistributeLoad(int relationships, String processorId) throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("Number of Relationships", "" + relationships);
    updateProcessor(processorId, "DistributeLoad", "0 sec", properties);
  }

  private void createDistributeLoadConnection(int relationships, String parentProcessGroupId,
      String destinationProcessGroup, String processorId,
      String inputPortId) throws IOException {
    String name = "" + relationships;
    connectSourceAndDestination(parentProcessGroupId, destinationProcessGroup,
        PROCESSOR.name(),
        INPUT_PORT.name(), processorId, inputPortId, Collections.singleton(name));
  }

  /**
   * Helper method to create a processor.
   *
   * @param parentProcessGroupId The id of the parent group where the processor will be created.
   * @param processorDTO A ProcessorDTO containing all needed values for the creation.
   * @return A ProcessorEntity object containing the newly created processor.
   */
  private ProcessorEntity createProcessor(@NotNull String parentProcessGroupId,
      ProcessorDTO processorDTO) throws IOException {
    return createProcessor(parentProcessGroupId, processorDTO, false);
  }

  private ProcessorEntity createProcessor(@NotNull String parentProcessGroupId,
      ProcessorDTO processorDTO, boolean isCommandHandler)
      throws IOException {

    PortEntity inputPort = findInputPort(parentProcessGroupId);

    ProcessorsEntity processorsOfGroup = getProcessorsOfGroup(parentProcessGroupId);
    Set<ProcessorEntity> processors = processorsOfGroup.getProcessors();

    List<Double> yPositions = processors.stream().filter(
            processorEntity -> processorEntity.getComponent().getType().equals(processorDTO.getType()))
        .map(processorEntity -> processorEntity.getPosition().getY()).collect(Collectors.toList());

    List<Double> xPositions = processors.stream().filter(
            processorEntity -> processorEntity.getComponent().getType().equals(processorDTO.getType()))
        .map(processorEntity -> processorEntity.getPosition().getX()).collect(Collectors.toList());

    Collections.sort(yPositions);
    Collections.sort(xPositions);

    double availablePositionY = 0;
    double availablePositionX = 0;

    if (inputPort == null) {
      ListIterator<Double> iterator = yPositions.listIterator();
      availablePositionY = yPositions.size();
      while (iterator.hasNext()) {
        double i = iterator.nextIndex();
        double next = iterator.next();
        double v = 150.0 * i;
        if (v != next) {
          availablePositionY = i;
          break;
        }
      }
    } else {
      ListIterator<Double> iteratorX = xPositions.listIterator();

      availablePositionX = yPositions.size();
      while (iteratorX.hasNext()) {
        int i = iteratorX.nextIndex();
        double next = iteratorX.next();
        double v = 550.0 * i;
        if (v != next) {
          availablePositionX = i;
          break;
        }
      }
    }

    RevisionDTO revisionDTO = createRevisionDTO();
    PositionDTO positionDTO = new PositionDTO();
    positionDTO.setX(availablePositionX * 550.0);
    positionDTO.setY(availablePositionY * 150.0);

    processorDTO.setPosition(positionDTO);

    ProcessorEntity processorEntity = new ProcessorEntity();
    processorEntity.setComponent(processorDTO);
    processorEntity.setRevision(revisionDTO);

    // Create processor.
    final CallReplyDTO callReplyDTO = postJSONCall(
        getNifiGroupByIdUrlPath(parentProcessGroupId) + "/" + PROCESSORS, processorEntity);

    // Return if processor creation was unsuccessfull.
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }

    processorEntity = mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);

    createConnections(parentProcessGroupId, processorEntity,
        processorDTO.getConfig().getAutoTerminatedRelationships() != null, isCommandHandler);

    return processorEntity;
  }

  /**
   * Helper method to create all needed connections of a processor..
   *
   * @param parentProcessGroupId The id of the parent group where the processor is located.
   * @param processorEntity A ProcessorEntity containing the processor that will be connected.
   * @return A ProcessorEntity object containing the newly connected processor.
   */
  private ProcessorEntity createConnections(String parentProcessGroupId,
      ProcessorEntity processorEntity,
      boolean autoTerminatedSuccess, boolean isCommandHandler) throws IOException {

    Set<String> connections = new HashSet<>();
    String processorId = processorEntity.getComponent().getId();

    List<RelationshipDTO> relationships = processorEntity.getComponent().getRelationships();
    List<String> failedRelationships = relationships.stream().filter(
            relationshipDTO -> EnumUtils.isValidEnum(FAILED_RELATIONSHIP_TYPES.class,
                relationshipDTO.getName().toUpperCase().replace("-", "_")))
        .map(RelationshipDTO::getName)
        .collect(Collectors.toList());

    PortEntity inputPort = findInputPort(parentProcessGroupId);

    if (!isCommandHandler) {
      ProcessorEntity distributeLoad = findDistributeLoad(parentProcessGroupId);
      if (distributeLoad != null) {
        int outgoingRelationships = getDistributeLoadOutgoingConnections(parentProcessGroupId,
            distributeLoad).size();

        if (outgoingRelationships == 0) {
          Set<ConnectionEntity> allConnectionsOfProcessGroup = getAllConnectionsOfProcessGroup(
              parentProcessGroupId);


        }

        outgoingRelationships += 1;
        updateDistributeLoad(outgoingRelationships, distributeLoad.getId());

        connectSourceAndDestination(parentProcessGroupId, parentProcessGroupId, PROCESSOR.name(),
            PROCESSOR.name(), distributeLoad.getId(), processorId,
            Collections.singleton(String.valueOf(outgoingRelationships)));

        changeProcessorStatus(distributeLoad.getComponent().getId(), STATE.RUNNING);

      } else if (inputPort != null) {
        connectSourceAndDestination(parentProcessGroupId, parentProcessGroupId, INPUT_PORT.name(),
            PROCESSOR.name(), inputPort.getId(), processorId, connections);
      }
    }

    OutputPortsEntity outputPorts = findOutputPorts(parentProcessGroupId);

    if (!outputPorts.getOutputPorts().isEmpty()) {
      if (!failedRelationships.isEmpty()) {
        connections = new HashSet<>(failedRelationships);
        Optional<PortEntity> logout = outputPorts.getOutputPorts().stream()
            .filter(portEntity -> portEntity.getComponent().getName().contains("_logout"))
            .findFirst();

        connectSourceAndDestination(parentProcessGroupId, parentProcessGroupId, PROCESSOR.name(),
            OUTPUT_PORT.name(), processorId, logout.get().getId(), connections);

      }

      if (!autoTerminatedSuccess && !isCommandHandler) {
        List<String> successful = relationships.stream().filter(
                relationshipDTO -> EnumUtils.isValidEnum(SUCCESSFUL_RELATIONSHIP_TYPES.class,
                    relationshipDTO.getName().toUpperCase())).map(RelationshipDTO::getName)
            .collect(Collectors.toList());

        connections = new HashSet<>(successful);
        Optional<PortEntity> out = outputPorts.getOutputPorts().stream()
            .filter(portEntity -> portEntity.getComponent().getName().contains("_out")).findFirst();

        connectSourceAndDestination(parentProcessGroupId, parentProcessGroupId, PROCESSOR.name(),
            OUTPUT_PORT.name(), processorId, out.get().getId(), connections);
      }
    }

    if (inputPort != null) {
      togglePort(inputPort, STATE.RUNNING, false);
    }

    outputPorts.getOutputPorts().forEach(portEntity -> {
      try {
        togglePort(portEntity,
            STATE.RUNNING, true);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    });

    return getProcessorById(processorId);
  }


  private List<ConnectionEntity> getDistributeLoadOutgoingConnections(String parentProcessGroupId,
      ProcessorEntity distributeLoad) throws IOException {
    return getAllConnectionsOfProcessor(
        distributeLoad.getId(), parentProcessGroupId).stream()
        .filter(connectionEntity -> connectionEntity.getSourceId().equals(distributeLoad.getId()))
        .collect(
            Collectors.toList());
  }

  private void togglePort(PortEntity portEntity, STATE state, boolean isOutputPort)
      throws IOException {

    PortRunStatusEntity portRunStatusEntity = new PortRunStatusEntity();
    portRunStatusEntity.setState(state.name());
    portRunStatusEntity.setRevision(portEntity.getRevision());

    CallReplyDTO callReplyDTO;
    if (isOutputPort) {
      callReplyDTO = putJSONCall("/output-ports/" + portEntity.getId() + RUN_STATUS,
          portRunStatusEntity);
    } else {
      callReplyDTO = putJSONCall("/input-ports/" + portEntity.getId() + RUN_STATUS,
          portRunStatusEntity);
    }

    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  /**
   * Helper method that connects a source with a destination.
   *
   * @param sourceProcessGroupId The id of the group where the source is located.
   * @param destinationProcessGroupId The id of the group where the destination is located.
   * @param sourceType The type of the source.
   * @param destinationType The type of the destination.
   * @param sourceId The id of the source.
   * @param destinationId The id of the destination.
   * @param relationships A set of all relationships that the connection is responsible for.
   */
  public void connectSourceAndDestination(String sourceProcessGroupId,
      String destinationProcessGroupId, String sourceType,
      String destinationType, String sourceId, String destinationId, Set<String> relationships)
      throws IOException {

    ConnectionEntity connectionEntity = new ConnectionEntity();
    ConnectionDTO connectionDTO = new ConnectionDTO();

    ConnectableDTO sourceDTO = new ConnectableDTO();
    sourceDTO.setGroupId(sourceProcessGroupId);
    sourceDTO.setType(sourceType);
    sourceDTO.setId(sourceId);

    ConnectableDTO destinationDTO = new ConnectableDTO();
    destinationDTO.setGroupId(destinationProcessGroupId);
    destinationDTO.setType(destinationType);
    destinationDTO.setId(destinationId);

    if (relationships != null) {
      connectionDTO.setSelectedRelationships(relationships);
    }

    connectionDTO.setSource(sourceDTO);
    connectionDTO.setDestination(destinationDTO);
    connectionEntity.setComponent(connectionDTO);
    connectionEntity.setRevision(createRevisionDTO());

    CallReplyDTO callReplyDTO = postJSONCall(
        getNifiGroupByIdUrlPath(sourceProcessGroupId) + "/connections",
        connectionEntity);
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  private ProcessorEntity updateProcessor(String processorId, String name, String schedulingPeriod,
      Map<String, String> properties)
      throws IOException {

    ProcessorEntity latestEntity = getProcessorById(processorId);
    String latestState = latestEntity.getComponent().getState();

    if (latestState.equals(STATE.RUNNING.name())) {
      latestEntity = changeProcessorStatus(processorId, STATE.STOPPED);
    }

    ProcessorEntity processorEntity = updateBuilder(latestEntity, processorId, name,
        schedulingPeriod, properties);

    CallReplyDTO callReplyDTO = putJSONCall("/" + PROCESSORS + "/" + processorId, processorEntity);

    if (callReplyDTO.isSuccessful()) {
      if (latestState.equals(STATE.RUNNING.name())) {
        return changeProcessorStatus(processorId, STATE.RUNNING);
      }
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  private ProcessorEntity updateBuilder(ProcessorEntity latestEntity, String processorId,
      String name, String schedulingPeriod,
      Map<String, String> properties) {
    Long currentVersion = latestEntity.getRevision().getVersion();

    ProcessorDTO processorDTO = new ProcessorDTO();
    processorDTO.setId(processorId);
    processorDTO.setName(name);

    ProcessorConfigDTO processorConfigDTO = new ProcessorConfigDTO();
    processorConfigDTO.setSchedulingPeriod(schedulingPeriod);

    if (properties.size() > 0) {
      processorConfigDTO.setProperties(properties);
    }

    processorDTO.setConfig(processorConfigDTO);

    RevisionDTO revisionDTO = createRevisionDTO();
    revisionDTO.setVersion(currentVersion);
    revisionDTO.setClientId(processorId);

    ProcessorEntity processorEntity = new ProcessorEntity();
    processorEntity.setRevision(revisionDTO);
    processorEntity.setComponent(processorDTO);

    return processorEntity;
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

    final CallReplyDTO callReplyDTO =
        putJSONCall("/" + PROCESSORS + "/" + processorId + "/run-status/",
            processorRunStatusEntity);

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public void moveComponent(String processGroupId, String processorId) throws IOException {
    ProcessorEntity processorToMove = getProcessorById(processorId);
    List<ConnectionEntity> allConnections = getAllConnectionsOfProcessor(processorId,
        processGroupId);

    Optional<ConnectionEntity> optionalCon = allConnections.stream()
        .filter(connectionEntity -> connectionEntity.getDestinationId().equals(processorId))
        .findFirst();

    if (optionalCon.isPresent()) {
      ConnectionEntity connectionEntity = optionalCon.get();
      String sourceId = connectionEntity.getComponent().getSource().getId();

      PositionDTO position = getProcessorById(sourceId).getComponent().getPosition();

      ProcessorEntity processorEntity = updateBuilder(processorToMove, processorId,
          processorToMove.getComponent().getName(),
          processorToMove.getComponent().getConfig().getSchedulingPeriod(), new HashMap<>());

      position.setY(position.getY() + 210);
      processorEntity.getComponent().setPosition(position);

      CallReplyDTO callReplyDTO = putJSONCall("/" + PROCESSORS + "/" + processorId,
          processorEntity);
      if (!callReplyDTO.isSuccessful()) {
        throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
      }
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

    String parentGroupId = processorEntity.getComponent().getParentGroupId();
    OutputPortsEntity outputPorts = findOutputPorts(parentGroupId);

    Set<ConnectionEntity> allConnections = getAllConnectionsOfProcessGroup(parentGroupId);
    PortEntity inputPort = findInputPort(parentGroupId);
    Optional<ConnectionEntity> optProcessorIncomingConnection = allConnections.stream()
        .filter(connectionEntity -> connectionEntity.getDestinationId().equals(processorId))
        .findFirst();
    ConnectionEntity processorIncomingConnection = optProcessorIncomingConnection.orElse(null);

    //Stop processor incoming connection.
    if (processorIncomingConnection != null) {
      String sourceType = processorIncomingConnection.getSourceType();
      if (INPUT_PORT.name().equals(sourceType)) {
        togglePort(inputPort, STATE.STOPPED, false);
      } else {
        changeProcessorStatus(processorIncomingConnection.getSourceId(), STATE.STOPPED);
      }
    }

    //Stop output port(s)
    outputPorts.getOutputPorts().forEach(portEntity -> {
      try {
        togglePort(portEntity, STATE.STOPPED, true);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    });

    //Get all connections of the processor
    List<ConnectionEntity> processorConnections = getAllConnectionsOfProcessor(processorId,
        parentGroupId);

    //Delete connections of the processor
    for (ConnectionEntity connectionEntity : processorConnections) {
      deleteConnection(connectionEntity);
    }

    CallReplyDTO callReplyDTO =
        deleteCall("/" + PROCESSORS + "/" + processorId
            + VERSION + processorEntity.getRevision().getVersion()
            + CLIENT_ID + processorId
            + "&disconnectedNodeAcknowledged=false");

    //Start processor incoming connection.
    if (processorIncomingConnection != null) {
      String sourceType = processorIncomingConnection.getSourceType();
      if (INPUT_PORT.name().equals(sourceType)) {
        Long version = inputPort.getRevision().getVersion();
        inputPort.getRevision().setVersion(version + 1L);
        boolean inputPortHasConnections = allConnections.stream().filter(
            connectionEntity -> connectionEntity.getSourceId().equals(inputPort.getId())).count()
            > 1L;
        if (inputPortHasConnections) {
          togglePort(inputPort, STATE.RUNNING, false);
        }
      } else {
        changeProcessorStatus(processorIncomingConnection.getSourceId(), STATE.RUNNING);
      }
    }

    //Start output port(s) if other connections exist.
    outputPorts.getOutputPorts().forEach(outputPort -> {
      boolean ouputPortHasConnections = allConnections.stream().filter(
              connectionEntity -> connectionEntity.getDestinationId().equals(outputPort.getId()))
          .count()
          > 1L;

      if (ouputPortHasConnections) {
        try {
          Long version = outputPort.getRevision().getVersion();
          outputPort.getRevision().setVersion(version + 1L);
          togglePort(outputPort, STATE.RUNNING, true);
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      }
    });

    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public Set<ConnectionEntity> getAllConnectionsOfProcessGroup(String parentGroupId)
      throws IOException {
    ProcessGroupFlowEntity processGroups = getProcessGroups(parentGroupId);
    return processGroups.getProcessGroupFlow().getFlow()
        .getConnections();
  }

  private List<ConnectionEntity> getAllConnectionsOfProcessor(String processorId,
      String parentGroupId)
      throws IOException {
    Set<ConnectionEntity> allConnections = getAllConnectionsOfProcessGroup(
        parentGroupId);

    return allConnections.stream()
        .filter(connectionEntity -> connectionEntity.getDestinationId().equals(processorId)
            || connectionEntity.getSourceId().equals(processorId))
        .collect(
            Collectors.toList());
  }

  public void emptyQueueOfConnection(String connectionId) throws IOException {
    postJSONCall("/flowfile-queues/" + connectionId + "/drop-requests", "");
  }

  public void emptyAllQueuesOfGroup(String groupId) throws IOException {
    postJSONCall("/process-groups/" + groupId + "/empty-all-connections-requests", "");
  }

  /**
   * Gets the validation errors of a processor.
   *
   * @param processorId The iid of the processor.
   * @return A Collection containing all the validation errors.
   */
  public Collection<String> getValidationErrors(String processorId) throws IOException {
    try {
      ProcessorEntity processor = getProcessorById(processorId);
      Collection<String> validationErrors = processor.getComponent().getValidationErrors();

      List<BulletinEntity> bulletins = processor.getBulletins();
      if (!bulletins.isEmpty()) {
        if (validationErrors == null) {
          validationErrors = new ArrayList<>();
        }
        validationErrors.add(bulletins.get(0).getBulletin().getMessage());
      }

      return validationErrors;
    } catch (NiFiProcessingException e) {
      return Collections.singletonList(NON_EXISTENT_PROCESSOR);
    }
  }

  public ProcessorEntity getProcessorById(String processorId) throws IOException {
    CallReplyDTO callReplyDTO = getCall("/" + PROCESSORS + "/" + processorId);
    if (callReplyDTO.isSuccessful()) {
      return mapper.readValue(callReplyDTO.getBody(), ProcessorEntity.class);
    } else {
      throw new NiFiProcessingException(callReplyDTO.getBody(), callReplyDTO.getCode());
    }
  }

  public boolean isProcessorRunning(String id) throws IOException {
    return getProcessorById(id).getStatus().getRunStatus()
        .equalsIgnoreCase(STATE.RUNNING.name());
  }

  public void deleteConnectionBetweenPortAndProcessor(PortEntity port, String processorId,
      ConnectionEntity connectionEntity) throws IOException {
    changePortStatus(port, STATE.STOPPED);
    changeProcessorStatus(processorId, STATE.STOPPED);
    deleteConnection(connectionEntity);
  }

  /**
   * Deletes a connection.
   *
   * @param connectionEntity the connection to delete.
   */
  private void deleteConnection(ConnectionEntity connectionEntity) throws IOException {
    emptyQueueOfConnection(connectionEntity.getId());

    CallReplyDTO callReplyDTO = deleteCall("/connections/" + connectionEntity.getId() + "?version"
        + "=" + connectionEntity.getRevision().getVersion());
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(),
          callReplyDTO.getCode());
    }
  }

  private void updateConnection(ConnectionEntity connectionEntity) throws IOException {
    CallReplyDTO callReplyDTO = putJSONCall("/connections/" + connectionEntity.getId(),
        connectionEntity);
    if (!callReplyDTO.isSuccessful()) {
      throw new NiFiProcessingException(callReplyDTO.getBody(),
          callReplyDTO.getCode());
    }
  }

  private RevisionDTO createRevisionDTO() {
    RevisionDTO revisionDTO = new RevisionDTO();
    revisionDTO.setVersion(0L);
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
