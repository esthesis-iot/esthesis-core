package esthesis.device.runtime.health;

import lombok.extern.java.Log;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A helper class to determine the IP addresses of a device.
 */
@Log
public class IPHelper {

  /**
   * A helper method to discover the IP address of the available interfaces.
   *
   * @param ifFilter A comma-separated list of interface names to include. If omitted, all
   * interfaces are included.
   */
  public static String getIPAddress(Optional<String> ifFilter) {
    try {
      return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
        .filter(net ->
          !ifFilter.isPresent() || StringUtils.isEmpty(ifFilter.get()) || Arrays
            .asList(ifFilter.get().split(",")).contains(net.getName()))
        .map(net -> net.getDisplayName() + ": " +
          Collections.list(net.getInetAddresses()).stream()
            .map(InetAddress::getHostAddress)
            .collect(Collectors.joining(","))
        ).collect(Collectors.joining("|"));
    } catch (SocketException e) {
      log.log(Level.WARNING, "Could not determine IP address");
      return "";
    }
  }
}
