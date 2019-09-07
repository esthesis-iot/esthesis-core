package esthesis.device.runtime.health;

import lombok.extern.java.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;

@Log
public class IPHelper {

  public static String getIPAddress() {
    try {
      String ips = "";

      Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
      for (NetworkInterface netint : Collections.list(nets)) {
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        ips += ", " + netint.getName() + ":";
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
          ips += " " + inetAddress.getHostAddress();
        }
      }
      ips.trim();
      if (ips.length() > 2) {
        return ips.substring(2);
      } else {
        return ips;
      }
    } catch (SocketException e) {
      log.log(Level.WARNING, "Could not determine IP address");
      return "";
    }
  }
}
