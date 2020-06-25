package esthesis.platform.server.service;

import com.eurodyn.qlack.common.exception.QCouldNotSaveException;
import com.eurodyn.qlack.util.data.optional.ReturnOptional;
import com.querydsl.core.types.Predicate;
import esthesis.platform.server.config.AppConstants.NIFI_SINK_HANDLER;
import esthesis.platform.server.config.NiFiSinkConfiguration;
import esthesis.platform.server.dto.nifisinks.NiFiLoggerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiProducerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiReaderFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiSinkDTO;
import esthesis.platform.server.dto.nifisinks.NiFiWriterFactoryDTO;
import esthesis.platform.server.model.NiFiSink;
import esthesis.platform.server.nifi.client.exception.NiFiProcessingException;
import esthesis.platform.server.nifi.client.util.NiFiConstants.PATH;
import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class NiFiSinkService extends BaseService<NiFiSinkDTO, NiFiSink> {

  public static final String ESTHESIS_NIFI_SINK_PACKAGE_PATH = "esthesis.platform"
    + ".server.nifi.sinks.";
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

  @Override
  public Page<NiFiSinkDTO> findAll(Predicate predicate, Pageable pageable) {
    Page<NiFiSinkDTO> sinks = super.findAll(predicate, pageable);

    sinks.forEach(niFiSinkDTO -> {
      try {
        niFiSinkDTO.setValidationErrors(validateNiFiSink(niFiSinkDTO,
          getNiFiSinkFactoryImplementation(niFiSinkDTO)));
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    });

    return sinks;
  }

  @Override
  public NiFiSinkDTO findById(long id) {
    NiFiSinkDTO niFiSinkDTO = super.findById(id);
    try {
      niFiSinkDTO.setValidationErrors(validateNiFiSink(niFiSinkDTO,
        getNiFiSinkFactoryImplementation(niFiSinkDTO)));
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    return niFiSinkDTO;
  }

  public NiFiSinkDTO saveSink(NiFiSinkDTO niFiSinkDTO) throws IOException {
    NiFiSinkFactory niFiSinkFactory = getNiFiSinkFactoryImplementation(niFiSinkDTO);

    String[] path = createPath(niFiSinkDTO, niFiSinkFactory);

    if (niFiSinkDTO.getId() == null) {
      niFiSinkDTO = createNiFiSink(niFiSinkDTO, niFiSinkFactory, path);
    } else {
      NiFiSink latestVersion = super.findEntityById(niFiSinkDTO.getId());
      niFiSinkDTO.setCustomInfo(latestVersion.getCustomInfo());

      if (!niFiSinkFactory.exists(niFiSinkDTO.getProcessorId())) {
        niFiSinkDTO = createNiFiSink(niFiSinkDTO, niFiSinkFactory, path);
      } else {
        updateNiFiSink(niFiSinkDTO, niFiSinkFactory, latestVersion);
      }
    }

    String sinkValidationErrors = validateNiFiSink(niFiSinkDTO, niFiSinkFactory);
    niFiSinkDTO.setValidationErrors(sinkValidationErrors);

    if (!StringUtils.isEmpty(sinkValidationErrors)) {
      niFiSinkDTO.setState(false);
    }

    return super.save(niFiSinkDTO);
  }

  private void updateNiFiSink(NiFiSinkDTO niFiSinkDTO, NiFiSinkFactory niFiSinkFactory,
    NiFiSink latestVersion) throws IOException {
    boolean isStateChanged = latestVersion.isState() != niFiSinkDTO.isState();
    boolean isConfigurationChanged = !latestVersion.getConfiguration()
      .equals(niFiSinkDTO.getConfiguration());

    //if sink is running, stop it before updating.
    if (latestVersion.isState() && isConfigurationChanged) {
      niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), false);
    }

    //update configuration if needed
    if (isConfigurationChanged) {
      try {
        niFiSinkFactory.updateSink(latestVersion, niFiSinkDTO);
      } catch (NiFiProcessingException e) {
        throw new QCouldNotSaveException("Could not save NiFi Sink.", e);
      }
    }

    //update state if needed
    if (isStateChanged || (isConfigurationChanged && latestVersion.isState())) {
      niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), niFiSinkDTO.isState());
    }
  }

  private NiFiSinkDTO createNiFiSink(NiFiSinkDTO niFiSinkDTO, NiFiSinkFactory niFiSinkFactory,
    String[] path) throws IOException {
    niFiSinkDTO = niFiSinkFactory.createSink(niFiSinkDTO, path);
    if (niFiSinkDTO.isState()) {
      niFiSinkFactory.toggleSink(niFiSinkDTO.getProcessorId(), niFiSinkDTO.isState());
    }
    return niFiSinkDTO;
  }

  private String validateNiFiSink(NiFiSinkDTO niFiSinkDTO, NiFiSinkFactory niFiSinkFactory)
    throws IOException {
    String sinkValidationErrors = niFiSinkFactory
      .getSinkValidationErrors(niFiSinkDTO.getProcessorId());
    return sinkValidationErrors;
  }

  public NiFiSinkDTO deleteSink(Long id) throws IOException {
    NiFiSinkDTO niFiSinkDTO = super.findById(id);
    NiFiSinkFactory niFiSinkFactory = getNiFiSinkFactoryImplementation(niFiSinkDTO);
    if (niFiSinkFactory.exists(niFiSinkDTO.getProcessorId())) {
      niFiSinkFactory.deleteSink(niFiSinkDTO);
    }

    return super.deleteById(id);
  }

  private NiFiSinkFactory getNiFiSinkFactoryImplementation(NiFiSinkDTO niFiSinkDTO) {
    return ReturnOptional.r(niFiSinkFactories.stream()
      .filter(n -> n.getClass().getName().equals(niFiSinkDTO.getFactoryClass()))
      .findFirst());
  }

  private String[] createPath(NiFiSinkDTO niFiSinkDTO, NiFiSinkFactory niFiSinkFactory) {
    String sinkType = "";

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
    String factoryTypePath = niFiSinkFactory.getClass().getName().replace(
      ESTHESIS_NIFI_SINK_PACKAGE_PATH, "");
    String[] splittedFactoryName = factoryTypePath.split("\\.");
    String factoryType =
      "[" + splittedFactoryName[1].toUpperCase().charAt(0) + "]";

    return new String[]{PATH.ESTHESIS.asString(), sinkType, handlerType, factoryType,
      PATH.INSTANCES.asString()};
  }

  public boolean isSynced() {
    List<NiFiSinkDTO> allSinks = findAll();
    List<NiFiSinkDTO> syncedSinks =
      allSinks.stream().filter(niFiSinkDTO -> {
        try {
          return getNiFiSinkFactoryImplementation(niFiSinkDTO).exists(niFiSinkDTO.getProcessorId());
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }).collect(Collectors.toList());

    return allSinks.size() == syncedSinks.size();
  }

  public boolean createAllMissingSinks() {
    List<NiFiSinkDTO> allSinks = findAll();

    allSinks.stream().forEach(niFiSinkDTO -> {
      NiFiSinkFactory niFiSinkFactoryImplementation = getNiFiSinkFactoryImplementation(niFiSinkDTO);
      try {
        if (!niFiSinkFactoryImplementation.exists(niFiSinkDTO.getProcessorId())) {
          saveSink(niFiSinkDTO);
        }
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    });

    return true;
  }

}
