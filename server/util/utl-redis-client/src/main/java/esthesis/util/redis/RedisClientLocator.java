package esthesis.util.redis;

import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.resource.DataflowSystemResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class RedisClientLocator {

  // Redis server URL is taken from the configuration of Dataflows, so the
  // resulting URL is pointing to the internal Kubernetes cluster IP. To
  // facilitate development, you can set the following property which will
  // override whatever value is discovered in runtime.
  @ConfigProperty(name = "redis.url")
  Optional<String> redisUrl;

  @Inject
  @RestClient
  DataflowSystemResource dataflowSystemResource;

  public Optional<RedisLocationDTO> getRedisLocation() {
    Optional<RedisLocationDTO> redisLocationDTO = Optional.empty();

    // Find which Redis server is being used.
    List<Dataflow> redisSetup = dataflowSystemResource.getRedisSetup();
    log.info("Redis servers registered: {}", redisSetup.size());
    if (redisSetup.size() > 0) {
      Dataflow redis = redisSetup.get(new Random().nextInt(redisSetup.size()));
      @SuppressWarnings("unchecked") String url = ((Map<String, String>) redis.getConfig()
          .get("redis")).get(
          "url");
      @SuppressWarnings("unchecked") String password = ((Map<String, String>) redis.getConfig()
          .get("redis")).get("password");

      if (redisUrl.isPresent()) {
        url = redisUrl.get();
      }

      Pattern p = Pattern.compile("(redis{1,2})://(.*):(.*)/(\\d*)",
          Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(url);
      boolean matchFound = m.find();
      if (!matchFound) {
        log.error("Redis URL '{}' is not valid, you need to provide a URL in "
            + "the form of redis://localhost:6379/0.", url);
      } else {
        String protocol = m.group(1);
        String host = m.group(2);
        int port = Integer.parseInt(m.group(3));
        int dbIndex = Integer.parseInt(m.group(4));

        redisLocationDTO = Optional.of(new RedisLocationDTO()
            .setUrl(url)
            .setProtocol(protocol)
            .setHost(host)
            .setPort(port)
            .setPassword(password)
            .setDbIndex(dbIndex));

        log.info("Found Redis server '{}'.", redisLocationDTO);
      }
    }

    return redisLocationDTO;
  }
}
