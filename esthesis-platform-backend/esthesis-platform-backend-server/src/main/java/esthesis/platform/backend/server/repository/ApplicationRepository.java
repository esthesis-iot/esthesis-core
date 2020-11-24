package esthesis.platform.backend.server.repository;

import com.eurodyn.qlack.util.querydsl.GenericQuerydslBinder;
import esthesis.platform.backend.server.model.Application;
import esthesis.platform.backend.server.model.QApplication;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends BaseRepository<Application>, QuerydslPredicateExecutor<Application>,
    GenericQuerydslBinder<QApplication> {

  @Override
  default void customize(QuerydslBindings bindings, QApplication application) {
    addGenericBindings(bindings);
  }

  Optional<Application> findByTokenEquals(String token);
}
