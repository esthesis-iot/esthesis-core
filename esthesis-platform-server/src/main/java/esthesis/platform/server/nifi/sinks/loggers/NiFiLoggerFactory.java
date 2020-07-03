package esthesis.platform.server.nifi.sinks.loggers;

import esthesis.platform.server.nifi.sinks.NiFiSinkFactory;

public interface NiFiLoggerFactory extends NiFiSinkFactory {

  /**
   * Returns whether logger is capable to write logs to Syslog.
   */
  boolean supportsSyslogLog();

  /**
   * Returns whether logger is capable to write logs to FileSystem.
   */
  boolean supportsFilesystemLog();

}
