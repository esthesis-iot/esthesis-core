package esthesis.backend.nifi.sinks.loggers;

import esthesis.backend.nifi.sinks.NiFiSinkFactory;

public interface NiFiLoggerFactory extends NiFiSinkFactory {

  /**
   * Returns whether logger is capable of writing logs to Syslog.
   */
  boolean supportsSyslogLog();

  /**
   * Returns whether logger is capable of writing logs to FileSystem.
   */
  boolean supportsFilesystemLog();

}
