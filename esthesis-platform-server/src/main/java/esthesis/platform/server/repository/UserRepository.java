package esthesis.platform.server.repository;

import esthesis.platform.server.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {

  Optional<User> findUserByEmail(String email);
}
