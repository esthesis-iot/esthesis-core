package esthesis.platform.server.nifi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiProcessFlowGroupDTO {

  private String id;
  private Double x;
  private Double y;
  private String name;
  private int runningCount;
  private int stoppedCount;
  private int invalidCount;
  private int disabledCount;
  private int staleCount;
  private int bytesIn;
  private int bytesQueued;
  private int bytesRead;
  private int bytesWritten;
  private int bytesOut;
  private int bytesTransferred;
  private int bytesReceived;
  private int bytesSent;
  private int activeThreadCount;

  @JsonProperty("position")
  private void unpackPosition(Map<String, Object> map) {
    x = (Double) map.get("x");
    y = (Double) map.get("y");
  }

  @JsonProperty("component")
  private void unpackComponent(Map<String, Object> map) {
    name = (String) map.get("name");
  }

  @JsonProperty("status")
  private void unpackStatus(Map<String, Object> map) {
    //noinspection unchecked
    Map<String, Object> stats = (HashMap<String, Object>) map.get("aggregateSnapshot");
    bytesIn = (int) stats.get("bytesIn");
    bytesQueued = (int) stats.get("bytesQueued");
    bytesRead = (int) stats.get("bytesRead");
    bytesWritten = (int) stats.get("bytesWritten");
    bytesOut = (int) stats.get("bytesOut");
    bytesTransferred = (int) stats.get("bytesTransferred");
    bytesReceived = (int) stats.get("bytesReceived");
    bytesSent = (int) stats.get("bytesSent");
    activeThreadCount = (int) stats.get("activeThreadCount");
  }
}
