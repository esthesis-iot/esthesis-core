package esthesis.util.redis.dto;

import esthesis.util.redis.RedisUtils.KeyType;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisMonitoredEntry implements Serializable {

	private KeyType keyType;
	private String key;
	private String field;
	private String value;
}
