package esthesis.service.common.paging;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Slf4j
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
    if (page != null && size != null) {
      return Optional.of(io.quarkus.panache.common.Page.of(page, size));
    }
    return Optional.empty();
  }

  public io.quarkus.panache.common.Sort getSortObject() {
    if (StringUtils.isNotBlank(sort)) {
      String[] items = sort.split(",");
      if (items.length % 2 != 0) {
        throw new IllegalArgumentException("Invalid sort parameters.");
      }

      io.quarkus.panache.common.Sort sorting = io.quarkus.panache.common.Sort.empty();
      for (int i = 0; i < items.length; i += 2) {
        String field = items[i];
        String direction = items[i + 1];
        if (StringUtils.isNotBlank(field) && StringUtils.isNotBlank(direction)) {
          if (direction.equalsIgnoreCase("asc")) {
            sorting.and(field, Sort.Direction.Ascending);
          } else if (direction.equalsIgnoreCase("desc")) {
            sorting.and(field, Sort.Direction.Descending);
          } else {
            throw new IllegalArgumentException("Invalid sort direction.");
          }
        }
      }
      return sorting;
    } else {
      return Sort.empty();
    }
  }

  public String getQueryKeys(boolean partialMatch) {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    StringBuilder queryKeys = new StringBuilder();
    queryParams.keySet().stream().sorted().forEach(key -> {
      if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
        if (key.endsWith("[]")) {
          key = key.substring(0, key.length() - 2);
          queryKeys.append(key).append(" in :p_").append(key).append(" and ");
        } else if (partialMatch) {
          queryKeys.append(key).append(" like :p_").append(key).append(" and ");
        } else {
          queryKeys.append(key).append(" = :p_").append(key).append(" and ");
        }
      }
    });

    // Remove final " and ".
    if (queryKeys.length() > 0) {
      queryKeys.delete(queryKeys.length() - " and ".length(), queryKeys.length());
    }

    return queryKeys.toString();
  }

  public Map<String, Object> getQueryValues() {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    Map<String, Object> queryValues = new HashMap<>();
    queryParams.keySet().stream().sorted().forEach(key -> {
      if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
        String val = queryParams.getFirst(key);
        if (val.startsWith("'") && val.endsWith("'")) {
          val = val.substring(1, val.length() - 1);
          queryValues.put("p_" + key, val);
        } else if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
          queryValues.put("p_" + key, Boolean.valueOf(val));
        } else if (val.startsWith("[") && val.endsWith("]") && key.endsWith("[]")) {
          val = val.substring(1, val.length() - 1);
          key = key.substring(0, key.length() - 2);
          queryValues.put("p_" + key, String.join(",", val.split("\\|")));
        } else {
          try {
            queryValues.put("p_" + key, Integer.parseInt(val));
          } catch (NumberFormatException e) {
            queryValues.put("p_" + key, val);
          }
        }
      }
    });

    return queryValues;
  }

  public boolean hasQuery() {
    return uriInfo.getQueryParameters().keySet().stream()
        .anyMatch(key -> !"page".equals(key) && !"size".equals(key) && !"sort".equals(key));
  }

  @Override
  public String toString() {
    return "Pageable(" + "uriInfo=" + uriInfo.getRequestUri() + ", page=" + page + ", size=" + size
        + ", sort=" + sort + ')';
  }
}
