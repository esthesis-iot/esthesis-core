package esthesis.platform.server.repository;

import com.eurodyn.qlack.common.util.KeyValue;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQuery;
import esthesis.platform.server.model.Audit;
import esthesis.platform.server.model.QAudit;
import esthesis.platform.server.model.QUser;
import esthesis.platform.server.model.User;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditRepository extends BaseRepository<Audit>,
  QuerydslPredicateExecutor<Audit>, QuerydslBinderCustomizer<QAudit>, AuditRepositoryExt {

  @Override
  default void customize(QuerydslBindings bindings, QAudit audit) {
    bindings.bind(audit.createdOn)
      .all((final DateTimePath<Instant> path, final Collection<? extends Instant> values) -> {
        final List<? extends Instant> dates = new ArrayList<>(values);
        Collections.sort(dates);
        if (dates.size() == 2) {
          return Optional.of(path.between(dates.get(0), dates.get(1)));
        } else {
          return Optional.of(path.eq(dates.get(0)));
        }
      });

    // Exclude fields from filter.
    bindings.excluding(audit.description);
  }
}

interface AuditRepositoryExt {

  List<String> findDistinctEvents();

  List<String> findDistinctLevels();

  List<User> findDistinctUsers();
}

class AuditRepositoryImpl implements AuditRepositoryExt {

  @PersistenceContext
  private EntityManager em;
  private final QAudit audit = QAudit.audit;
  private final QUser user = QUser.user;

  @Override
  public List<String> findDistinctEvents() {
    return new JPAQuery<String>(em)
      .distinct()
      .select(audit.event)
      .from(audit)
      .orderBy(audit.event.asc())
      .fetch();
  }

  @Override
  public List<String> findDistinctLevels() {
    return new JPAQuery<String>(em)
      .distinct()
      .select(audit.level)
      .from(audit)
      .orderBy(audit.level.asc())
      .fetch();
  }

  @Override
  public List<User> findDistinctUsers() {
    return
      new JPAQuery<KeyValue>(em)
        .distinct()
        .select(Projections.fields(User.class, user.id, user.fn, user.ln, user.email))
        .from(audit)
        .innerJoin(audit.user, user)
        .orderBy(user.fn.asc()).orderBy(user.ln.asc())
        .fetch();
  }
}
