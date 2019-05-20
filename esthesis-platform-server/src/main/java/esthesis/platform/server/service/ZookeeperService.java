package esthesis.platform.server.service;

import esthesis.platform.server.dto.ZookeeperServerDTO;
import esthesis.platform.server.mapper.ZookeeperServerMapper;
import esthesis.platform.server.model.ZookeeperServer;
import esthesis.platform.server.repository.ZookeeperServerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.logging.Logger;

@Service
@Validated
@Transactional
public class ZookeeperService extends BaseService<ZookeeperServerDTO, ZookeeperServer> {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ZookeeperService.class.getName());

  private final ZookeeperServerMapper zookeeperServerMapper;
  private final ZookeeperServerRepository zookeeperServerRepository;


  public ZookeeperService(ZookeeperServerMapper zookeeperServerMapper,
    ZookeeperServerRepository zookeeperServerRepository) {
    this.zookeeperServerMapper = zookeeperServerMapper;
    this.zookeeperServerRepository = zookeeperServerRepository;
  }

  public List<ZookeeperServerDTO> findActive() {
    return zookeeperServerMapper.map(zookeeperServerRepository.findAllByState(true));
  }

  @Override
  public ZookeeperServerDTO save(ZookeeperServerDTO dto) {
    // Save Zookeeper server configuration.
    dto = super.save(dto);

    // Return the object saved.
    return dto;
  }

  @Override
  public ZookeeperServerDTO deleteById(long id) {
    // Delete Zookeeper server.
    final ZookeeperServerDTO zookeeperServerDTO = super.deleteById(id);

    // Return the object deleted.
    return zookeeperServerDTO;
  }

}
