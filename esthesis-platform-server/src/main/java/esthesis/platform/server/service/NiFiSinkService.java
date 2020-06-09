package esthesis.platform.server.service;

import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import esthesis.platform.server.config.AppConstants.NIFI_SINK_HANDLER;
import esthesis.platform.server.config.NiFiSinkConfiguration;
import esthesis.platform.server.dto.nifisinks.NiFiLoggerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiProducerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiReaderFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.dto.nifisinks.NiFiWriterFactoryDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.util.NiFiConstants.PATH;
import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@Transactional

public class NiFiSinkService extends BaseService<NiFiSinkDTO, NiFiSink> {

  private final NiFiSinkConfiguration niFiSinkConfiguration;
  private final List<NiFiSinkFactory> niFiSinkFactories;

  public NiFiSinkService(NiFiSinkConfiguration niFiSinkConfiguration,
    List<NiFiSinkFactory> niFiSinkFactories) {
    this.niFiSinkConfiguration = niFiSinkConfiguration;
    this.niFiSinkFactories = niFiSinkFactories;
  }

  public List<NiFiReaderFactoryDTO> findAvailableNiFiReaderFactories() {
    return niFiSinkConfiguration.getAvailableReaders();
  }

  public List<NiFiWriterFactoryDTO> findAvailableNiFiWriterFactories() {
    return niFiSinkConfiguration.getAvailableWriters();
  }

  public List<NiFiProducerFactoryDTO> findAvailableNiFiProducerFactories() {
    return niFiSinkConfiguration.getAvailableProducers();
  }

  public List<NiFiLoggerFactoryDTO> findAvailableNiFiLoggerFactories() {
    return new ArrayList<>(); //niFiSinkConfiguration.getAvailableLoggers();
  }

  public NiFiSinkDTO saveSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
    NiFiSinkFactory niFiSinkFactory = getNiFiSinkFactoryImplementation(niFiSinkDTO);

    String[] path = createPath(niFiSinkDTO, niFiSinkFactory);

    if (niFiSinkDTO.getId() == null) {
      niFiSinkDTO = niFiSinkFactory.createSink(niFiSinkDTO, path);
      if (niFiSinkDTO.isState()) {
        niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), niFiSinkDTO.isState());
      }
    } else {
      NiFiSink latestVersion = super.findEntityById(niFiSinkDTO.getId());
      niFiSinkDTO.setCustomInfo(latestVersion.getCustomInfo());

      boolean isStateChanged = latestVersion.isState() != niFiSinkDTO.isState();
      boolean isConfigurationChanged = !latestVersion.getConfiguration()
        .equals(niFiSinkDTO.getConfiguration());

      //if sink is running, stop it before updating.
      if (latestVersion.isState() && isConfigurationChanged) {
        niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), false);
      }

      //update configuration if needed
      if (isConfigurationChanged) {
        niFiSinkFactory.updateSink(latestVersion, niFiSinkDTO);
      }

      //update state if needed
      if (isStateChanged || (isConfigurationChanged && latestVersion.isState())) {
        niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), niFiSinkDTO.isState());
      }
    }

    String sinkValidationErrors = niFiSinkFactory.getSinkValidationErrors(
      niFiSinkDTO.getProcessorId());
    niFiSinkDTO.setValidationErrors(sinkValidationErrors);

    if (!StringUtils.isEmpty(sinkValidationErrors)) {
      niFiSinkDTO.setState(false);
    }

    return super.save(niFiSinkDTO);
  }

  public NiFiSinkDTO deleteSink(Long id) throws IOException {
    NiFiSinkDTO niFiSinkDTO = super.findById(id);
    NiFiSinkFactory niFiSinkFactory = getNiFiSinkFactoryImplementation(niFiSinkDTO);
    niFiSinkFactory.deleteSink(niFiSinkDTO.getProcessorId());

    return super.deleteById(id);
  }

  private NiFiSinkFactory getNiFiSinkFactoryImplementation(NiFiSinkDTO niFiSinkDTO) {
    return ReturnOptional.r(niFiSinkFactories.stream()
      .filter(n -> n.getClass().getName().equals(niFiSinkDTO.getFactoryClass()))
      .findFirst());
  }

  private String[] createPath(NiFiSinkDTO niFiSinkDTO, NiFiSinkFactory niFiSinkFactory) {
    String sinkType = PATH.CONSUMERS.asString();

    for (PATH value : PATH.values()) {
      if (value.asString().indexOf(niFiSinkDTO.getType().toUpperCase().charAt(0)) > -1) {
        sinkType = value.asString();
      }
    }

    String handlerType = "";
    for (NIFI_SINK_HANDLER value : NIFI_SINK_HANDLER.values()) {
      if (value.getType() == niFiSinkDTO.getHandler()) {
        handlerType = "[" + value.name().toUpperCase().charAt(0) + sinkType.substring(1);
      }
    }

    String[] splittedFactoryName = niFiSinkFactory.getClass().getName().split("\\.");
    String factoryType =
      "[" + splittedFactoryName[splittedFactoryName.length - 2].toUpperCase().charAt(0) + "]";

    return new String[]{PATH.ESTHESIS.asString(), sinkType, handlerType, factoryType,
      PATH.INSTANCES.asString()};
  }
}
