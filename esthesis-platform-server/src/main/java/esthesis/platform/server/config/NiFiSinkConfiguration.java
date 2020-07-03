package esthesis.platform.server.config;

import esthesis.platform.server.dto.nifisinks.NiFiLoggerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiProducerFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiReaderFactoryDTO;
import esthesis.platform.server.dto.nifisinks.NiFiWriterFactoryDTO;
import esthesis.platform.server.nifi.sinks.loggers.NiFiLoggerFactory;
import esthesis.platform.server.nifi.sinks.producers.NiFiProducerFactory;
import esthesis.platform.server.nifi.sinks.readers.NiFiReaderFactory;
import esthesis.platform.server.nifi.sinks.writers.NiFiWriterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class NiFiSinkConfiguration {

  private final List<NiFiReaderFactory> niFiReaderFactories;
  private final List<NiFiWriterFactory> niFiWriterFactories;
  private final List<NiFiProducerFactory> niFiProducerFactories;
  private final List<NiFiLoggerFactory> niFiLoggerFactories;

  public NiFiSinkConfiguration(
    List<NiFiReaderFactory> niFiReaderFactories,
    List<NiFiWriterFactory> niFiWriterFactories,
    List<NiFiProducerFactory> niFiProducerFactories,
    List<NiFiLoggerFactory> niFiLoggerFactories) {
    this.niFiReaderFactories = niFiReaderFactories;
    this.niFiWriterFactories = niFiWriterFactories;
    this.niFiProducerFactories = niFiProducerFactories;
    this.niFiLoggerFactories = niFiLoggerFactories;
  }


  @Bean
  public List<NiFiReaderFactoryDTO> getAvailableReaders() {
    List<NiFiReaderFactoryDTO> niFiReaderFactoryDTOS = new ArrayList<>();
    niFiReaderFactories.stream().forEach(niFiReaderFactory ->
      niFiReaderFactoryDTOS.add(NiFiReaderFactoryDTO.builder()
        .factoryClass(niFiReaderFactory.getClass().getName())
        .friendlyName(niFiReaderFactory.getFriendlyName())
        .supportsPingRead(niFiReaderFactory.supportsPingRead())
        .supportsMetadataRead(niFiReaderFactory.supportsMetadataRead())
        .supportsTelemetryRead(niFiReaderFactory.supportsTelemetryRead())
        .configurationTemplate(niFiReaderFactory.getConfigurationTemplate())
        .build()));
    return niFiReaderFactoryDTOS;
  }

  @Bean
  public List<NiFiWriterFactoryDTO> getAvailableWriters() {

    List<NiFiWriterFactoryDTO> niFiWriterFactoryDTOS = new ArrayList<>();
    niFiWriterFactories.stream().forEach(niFiWriterFactory ->
      niFiWriterFactoryDTOS.add(NiFiWriterFactoryDTO.builder()
        .factoryClass(niFiWriterFactory.getClass().getName())
        .friendlyName(niFiWriterFactory.getFriendlyName())
        .supportsPingWrite(niFiWriterFactory.supportsPingWrite())
        .supportsMetadataWrite(niFiWriterFactory.supportsMetadataWrite())
        .supportsTelemetryWrite(niFiWriterFactory.supportsTelemetryWrite())
        .configurationTemplate(niFiWriterFactory.getConfigurationTemplate())
        .build()));

    return niFiWriterFactoryDTOS;
  }

  @Bean
  public List<NiFiProducerFactoryDTO> getAvailableProducers() {

    List<NiFiProducerFactoryDTO> niFiProducerFactoryDTOS = new ArrayList<>();
    niFiProducerFactories.stream().forEach(niFiProducerFactory ->
      niFiProducerFactoryDTOS
        .add(NiFiProducerFactoryDTO.builder().factoryClass(niFiProducerFactory.getClass().getName())
          .friendlyName(niFiProducerFactory.getFriendlyName())
          .supportsTelemetryProduce(niFiProducerFactory.supportsTelemetryProduce())
          .configurationTemplate(niFiProducerFactory.getConfigurationTemplate())
          .build()));

    return niFiProducerFactoryDTOS;
  }

  @Bean
  public List<NiFiLoggerFactoryDTO> getAvailableLoggers() {

    List<NiFiLoggerFactoryDTO> niFiLoggerFactoryDTOS = new ArrayList<>();
    niFiLoggerFactories.stream().forEach(niFiLoggerFactory ->
      niFiLoggerFactoryDTOS
        .add(NiFiLoggerFactoryDTO.builder().factoryClass(niFiLoggerFactory.getClass().getName())
          .friendlyName(niFiLoggerFactory.getFriendlyName())
          .supportsFilesystemLog(niFiLoggerFactory.supportsFilesystemLog())
          .supportsSyslogLog(niFiLoggerFactory.supportsSyslogLog())
          .configurationTemplate(niFiLoggerFactory.getConfigurationTemplate())
          .build()));

    return niFiLoggerFactoryDTOS;
  }

}
