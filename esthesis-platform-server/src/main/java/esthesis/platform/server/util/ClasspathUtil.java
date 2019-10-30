package esthesis.platform.server.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClasspathUtil {

  /**
   * Returns the content of a text resource from the classpath. Works during development as well as
   * in fat JARs.
   *
   * @param resource The name of the file to read.
   */
  public static String readFromClasspath(String resource)
  throws IOException {
    ClassPathResource classPathResource = new ClassPathResource(resource);
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(
      new InputStreamReader(classPathResource.getInputStream()))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

}
