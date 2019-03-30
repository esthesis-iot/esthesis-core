package esthesis.platform.server.service;

import esthesis.platform.common.config.AppConstants.Generic;
import esthesis.platform.server.dto.RedisServerDTO;
import esthesis.platform.server.mapper.RedisServerMapper;
import esthesis.platform.server.model.RedisServer;
import esthesis.platform.server.repository.RedisServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional
@Validated
public class RedisServerService extends BaseService<RedisServerDTO, RedisServer> {

  private final RedisServerMapper redisServerMapper;
  private final RedisServerRepository redisServerRepository;

  public RedisServerService(RedisServerMapper redisServerMapper,
      RedisServerRepository redisServerRepository) {
    this.redisServerMapper = redisServerMapper;
    this.redisServerRepository = redisServerRepository;
  }

  public List<RedisServerDTO> findActive() {
    return redisServerMapper.map(redisServerRepository.findAllByState(Generic.ENABLED));
  }
}
