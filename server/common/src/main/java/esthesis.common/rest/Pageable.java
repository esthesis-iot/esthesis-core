package esthesis.common.rest;

import io.quarkus.panache.common.Sort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Slf4j
@ToString
@NoArgsConstructor
public class Pageable {

  @Context
  private UriInfo uriInfo;

  @QueryParam("page")
  public Integer page;

  @QueryParam("size")
  public Integer size;

  @QueryParam("sort")
  public String sort;

  public Optional<io.quarkus.panache.common.Page> getPageObject() {
    if (page != null & size != null) {
      return Optional.of(io.quarkus.panache.common.Page.of(page, size));
    }
    return Optional.empty();
  }

  public io.quarkus.panache.common.Sort getSortObject() {
    if (StringUtils.isNotBlank(sort)) {
      if (getSort().split(",")[1].equals("asc")) {
        return io.quarkus.panache.common.Sort.ascending(
            getSort().split(",")[0]);
      } else {
        return io.quarkus.panache.common.Sort.descending(
            getSort().split(",")[0]);
      }
    } else {
      return Sort.empty();
    }
  }

  public String getQueryKeys(boolean partialMatch) {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    StringBuilder queryKeys = new StringBuilder();
    queryParams.keySet().stream().sorted().forEach(key -> {
      if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
        if (partialMatch) {
          queryKeys.append(key).append(" like :p_").append(key).append(",");
        } else {
          queryKeys.append(key).append(" = :p_").append(key).append(",");
        }
      }
    });

    if (queryKeys.length() > 0) {
      queryKeys.deleteCharAt(queryKeys.length() - 1);
    }

    return queryKeys.toString();
  }

  public Map<String, Object> getQueryValues() {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    Map<String, Object> queryValues = new HashMap<>();
    queryParams.keySet().stream().sorted().forEach(key -> {
      if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
        queryValues.put("p_" + key, queryParams.getFirst(key));
      }
    });

    return queryValues;
  }

  public boolean hasQuery() {
    return uriInfo.getQueryParameters().keySet().stream().anyMatch(
        key -> !"page".equals(key) && !"size".equals(key) && !"sort".equals(
            key));
  }

}
