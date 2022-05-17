package esthesis.platform.server.nifi.sinks.loggers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

import java.io.IOException;

public interface NiFiLoggerFactory extends NiFiSinkFactory {

  /**
   * Returns whether logger is capable of writing logs to Syslog.
   */
  boolean supportsSyslogLog();

  /**
   * Returns whether logger is capable of writing logs to FileSystem.
   */
  boolean supportsFilesystemLog();

  /**
   *  Creates/deletes the connection to the Clear Queue processor.
   *  Connection is created when no loggers of specific type are available and deleted when
   *  at least one exists.
   * @param path The path to the Instance group of the logger type.
   * @throws IOException
   */
  void manageConnectionWithClearQueueProcessor(String[] path) throws IOException;
}
