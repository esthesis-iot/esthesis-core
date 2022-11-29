package esthesis.util.redis;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class RedisUtils {

  @Inject
  RedisDataSource redis;

  private HashCommands<String, String, String> hashCommandText;
  private HashCommands<String, String, byte[]> hashCommandBinary;

  @PostConstruct
  void init() {
    hashCommandText = redis.hash(String.class);
    hashCommandBinary = redis.hash(byte[].class);
  }
}
