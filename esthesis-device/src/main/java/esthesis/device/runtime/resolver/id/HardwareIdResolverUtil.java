package esthesis.device.runtime.resolver.id;

import com.eurodyn.qlack.fuse.crypto.CryptoDigestService;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HardwareIdResolverUtil {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(HardwareIdResolverUtil.class.getName());

  private static final String FILE_PREFIX = "file:";
  private static final String DEVICE_PREFIX = "device:";
  private static final String RESOLVE_CLASS_METHOD = "resolve";
  private static final String RESOLVE_CLASS_PACKAGE = "esthesis.device.runtime.resolver.id.";
  private static String deviceId;
  private final static CryptoDigestService cryptoDigestService = new CryptoDigestService();

  /**
   * Returns the registration id of the device on which the runtime agent runs. IDs are resolved by
   * different types of resolvers that support well-known hardware capable of providing a unique
   * ID.
   */
  public static String resolve(String hardwareId)
  throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
    if (StringUtils.isEmpty(deviceId)) {
      LOGGER.log(Level.FINEST, "Registration ID to resolve: {0}", hardwareId);
      if (hardwareId.startsWith(FILE_PREFIX)) {
        String filename = hardwareId.substring(FILE_PREFIX.length());
        deviceId = FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8);
      } else if (hardwareId.startsWith(DEVICE_PREFIX)) {
        String hardware = hardwareId.substring(DEVICE_PREFIX.length());
        String resolverClass = RESOLVE_CLASS_PACKAGE + hardware;
        LOGGER.log(Level.FINEST, "Using ID resolver class: {0}.", resolverClass);
        deviceId = (String) ReflectionUtils.invokeMethod(
          ReflectionUtils
            .findMethod(Class.forName(resolverClass), RESOLVE_CLASS_METHOD, String.class),
          Class.forName(resolverClass).newInstance(), deviceId);
      } else {
        deviceId = hardwareId;
      }
      LOGGER.log(Level.FINE, "Resolved ID: {0}.", deviceId);
    }

    return deviceId;
  }

  public static String shell(String[] cmd) throws IOException, InterruptedException {
    StringBuilder output = new StringBuilder();

    Process p = Runtime.getRuntime().exec(cmd);
    p.waitFor(10, TimeUnit.SECONDS);
    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      output.append(line).append("\n");
    }

    return output.toString();
  }

  public static String md5(String text) {
    return cryptoDigestService.md5(text);
  }
}
