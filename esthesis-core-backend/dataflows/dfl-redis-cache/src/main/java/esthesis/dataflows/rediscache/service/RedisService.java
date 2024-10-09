package esthesis.dataflows.rediscache.service;

import esthesis.common.avro.EsthesisDataMessage;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.core.common.AppConstants;
import esthesis.dataflows.rediscache.config.AppConfig;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Transactional
@ApplicationScoped
public class RedisService {

	@Inject
	AppConfig conf;

	@Inject
	RedisUtils redisUtils;

	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String VALUE_TYPE_FIELD_NAME = "valueType";

	public void process(Exchange exchange) {
		// Get the message from the exchange.
		EsthesisDataMessage esthesisMessage = exchange.getIn()
			.getBody(EsthesisDataMessage.class);

		// Write the key to Redis.
		esthesisMessage.getPayload().getValues().forEach(keyValue -> {
			String key = esthesisMessage.getHardwareId();
			String fieldName = String.join(".",
				esthesisMessage.getPayload().getCategory(), keyValue.getName());
			String fieldValue = keyValue.getValue();
			ValueType fieldType = ValueType.valueOf(keyValue.getValueType().name());

			if (fieldValue.length() <= conf.redisMaxSize()) {
				redisUtils.setToHash(KeyType.ESTHESIS_DM, key, fieldName, fieldValue);
				redisUtils.setToHash(KeyType.ESTHESIS_DM, key, String.join(".", fieldName,
						TIMESTAMP_FIELD_NAME),
					esthesisMessage.getPayload().getTimestamp());
				redisUtils.setToHash(KeyType.ESTHESIS_DM, key, String.join(".", fieldName,
					VALUE_TYPE_FIELD_NAME), fieldType.name());
			} else {
				log.debug("Value '{}' for '{}' is too long, skipping caching. Current maximum"
						+ " value size is '{}' bytes.",
					StringUtils.abbreviate(fieldValue, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH), key,
					conf.redisMaxSize());
			}

			// Expire hash, if requested.
			if (conf.redisTtl() > 0) {
				redisUtils.setExpirationForHash(KeyType.ESTHESIS_DM, key, conf.redisTtl() * 60);
			}
		});
	}
}
