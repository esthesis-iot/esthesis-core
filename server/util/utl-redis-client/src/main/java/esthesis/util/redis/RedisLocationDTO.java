package esthesis.util.redis;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class RedisLocationDTO {

  String url;
  String protocol;
  String host;
  int port;
  int dbIndex;
  String password;
}
