package esthesis.service.common.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * A representation of a pageable request.
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Pageable {

	@QueryParam("page")
	public Integer page;

	@QueryParam("size")
	public Integer size;

	@QueryParam("sort")
	public String sort;

	@Context
	@JsonIgnore
	private UriInfo uriInfo;

	/**
	 * Helper method to create an empty Pageable object.
	 */
	public static Pageable empty() {
		Pageable pageable = new Pageable();
		pageable.setPage(0);
		pageable.setSize(Integer.MAX_VALUE);
		pageable.setSort("");
		return pageable;
	}

	public Optional<io.quarkus.panache.common.Page> getPageObject() {
		if (page != null && size != null) {
			return Optional.of(io.quarkus.panache.common.Page.of(page, size));
		}
		return Optional.empty();
	}

	public io.quarkus.panache.common.Sort getSortObject() {
		if (StringUtils.isBlank(sort)) {
			return Sort.empty();
		}

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
	}

	/**
	 * Returns the keys comprising the query parameters.
	 * <p>
	 * Anything in the URI except 'page', 'size' and 'sort' is considered a query parameter. Key names
	 * can have special suffixes to aid in constructing specific query types:
	 * <ul>
	 * <li>[]: Signifies a key that will be rendered with an 'in' clause.</li>
	 * <li>>, >=, <, <=: Signifies a key that will be rendered with a 'greater than',
	 * 'greater than or equal to', etc. </li>
	 * <li>*: Indicates a partial match, rendered with a 'like' operator.</li>
	 * </ul>
	 *
	 * @return A string representing the keys of the query parameters.
	 */
	@SuppressWarnings("java:S1192")
	public String getQueryKeys() {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		log.trace("Query params: {}", queryParams);
		StringBuilder queryKeys = new StringBuilder();
		queryParams.keySet().stream().sorted().forEach(key -> {
			if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
				String keyHash = String.valueOf(key.hashCode()).replace("-", "");
				if (key.endsWith("[]")) {
					key = key.substring(0, key.length() - 2);
					queryKeys.append(key).append(" in :v_").append(keyHash).append(" and ");
				} else if (key.endsWith(">")) {
					key = key.substring(0, key.length() - 1);
					queryKeys.append(key).append(" > :v_").append(keyHash).append(" and ");
				} else if (key.endsWith(">=")) {
					key = key.substring(0, key.length() - 2);
					queryKeys.append(key).append(" >= :v_").append(keyHash).append(" and ");
				} else if (key.endsWith("<")) {
					key = key.substring(0, key.length() - 1);
					queryKeys.append(key).append(" < :v_").append(keyHash).append(" and ");
				} else if (key.endsWith("<=")) {
					key = key.substring(0, key.length() - 2);
					queryKeys.append(key).append(" <= :v_").append(keyHash).append(" and ");
				} else if (key.endsWith("*")) {
					queryKeys.append(StringUtils.stripEnd(key, "*")).append(" like :v_").append(keyHash)
						.append(" and ");
				} else {
					queryKeys.append(key).append(" = :v_").append(keyHash).append(" and ");
				}
			}
		});

		// Remove final " and ".
		if (!queryKeys.isEmpty()) {
			queryKeys.delete(queryKeys.length() - " and ".length(), queryKeys.length());
		}

		log.trace("Query keys: {}", queryKeys);
		return queryKeys.toString();
	}

	/**
	 * Returns the values comprising the query parameters.
	 * <p>
	 * Anything in the URI except 'page', 'size' and 'sort' is considered a query parameter. Key
	 * values can have special suffixes to aid in constructing specific query types: - []: Signifies
	 * <ul>
	 *   <li>'': A value enclosed in single quotes will be rendered as a string. This is a
	 *   guidance you can provide to the builder here in case your value may match other value
	 *   type cases otherwise. For example, if you want to search by the text representation of e.g
	 *   6, you can pass this parameter as '6'.
	 *   </li>
	 *   <li>true/false: The value will be treated as a Boolean value.</li>
	 *   <li>[]: A value enclosed in square brackets will be treated as a list (remember to also
	 *   add a [] suffix to the respective key name). The list of values should be
	 *   comma-separated.</li>
	 * </ul>
	 * <p>
	 * If none of the above cases are present for the value of a key, this algorithm will attempt
	 * to interpret the value first as a number, then as a date (using ISO 8601 format), and
	 * finally just use the value verbatim.
	 *
	 * @return A map representing the values of the query parameters.
	 */
	@SuppressWarnings("java:S3776")
	public Map<String, Object> getQueryValues() {
		if (uriInfo != null) {
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			Map<String, Object> queryValues = new HashMap<>();
			queryParams.keySet().stream().sorted().forEach(key -> {
				String keyHash = String.valueOf(key.hashCode()).replace("-", "");
				if (!"page".equals(key) && !"size".equals(key) && !"sort".equals(key)) {
					String val = queryParams.getFirst(key);
					if (val.startsWith("'") && val.endsWith("'")) {
						val = val.substring(1, val.length() - 1);
						queryValues.put("v_" + keyHash, val);
					} else if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
						queryValues.put("v_" + keyHash, Boolean.valueOf(val));
					} else if (val.startsWith("[") && val.endsWith("]") && key.endsWith("[]")) {
						val = val.substring(1, val.length() - 1);
						queryValues.put("v_" + keyHash, String.join(",", val.split("\\|")));
					} else {
						try {
							queryValues.put("v_" + keyHash, Integer.parseInt(val));
						} catch (NumberFormatException e1) {
							try {
								queryValues.put("v_" + keyHash, Instant.parse(val));
							} catch (DateTimeParseException e2) {
								queryValues.put("v_" + keyHash, val);
							}
						}
					}
				}
			});

			log.trace("Query values: {}", queryValues);
			return queryValues;
		} else {
			return new HashMap<>();
		}
	}

	/**
	 * Returns whether the request has a query defined.
	 *
	 * @return True if the request has a query defined, false otherwise.
	 */
	public boolean hasQuery() {
		if (uriInfo != null) {
			return uriInfo.getQueryParameters().keySet().stream()
				.anyMatch(key -> !"page".equals(key) && !"size".equals(key) && !"sort".equals(key));
		} else {
			return false;
		}
	}

	/**
	 * Custom toString method for printing a Pageable object.
	 *
	 * @return A string representation of the Pageable object.
	 */
	@Override
	public String toString() {
		return "Pageable(" + "uriInfo=" + uriInfo.getRequestUri() + ", page=" + page + ", size=" + size
			+ ", sort=" + sort + ')';
	}
}
