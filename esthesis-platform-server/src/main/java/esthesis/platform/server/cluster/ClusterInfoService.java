package esthesis.platform.server.cluster;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class ClusterInfoService {

  // A flag indicating whether the node runs in standalone or cluster mode. In standalone
  // mode no connectivity to a Zookeeper node is active.
  @Getter
  @Setter
  private boolean standalone;

  // A flag indicating whether the node is the current cluster leader. A cluster leader is able
  // to run tasks that need to be executed only in one place (or once) within the cluster.
  @Getter
  @Setter
  private boolean isClusterLeader;

}
