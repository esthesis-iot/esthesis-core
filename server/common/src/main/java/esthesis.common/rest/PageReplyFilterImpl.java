package esthesis.common.rest;

import ch.mfrey.jackson.antpathfilter.AntPathFilterMixin;
import ch.mfrey.jackson.antpathfilter.AntPathPropertyFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * See @{@link PageReplyFilter}.
 */
@Slf4j
@Provider
@PageReplyFilter
@Priority(Priorities.HEADER_DECORATOR)
public class PageReplyFilterImpl implements ContainerResponseFilter {

  @Context
  private ResourceInfo info;

  @Inject
  ObjectMapper injectedMapper;

  private static final String[] DEFAULT_PAGE_FILTER = new String[]
      {"page", "size", "totalElements"};
  private static final String[] DEFAULT_OBJECT_FILTER = new String[]{};

  @Override
  public void filter(ContainerRequestContext req,
      ContainerResponseContext res) throws JsonProcessingException {
    // Clone Quarkus ObjectMapper to get a fully-configured mapper, also to avoid
    // mutating the global mapper.
    ObjectMapper clonedMapper = injectedMapper.copy();

    // Get filter elements provided in the annotation and construct the basis
    // of the filter.
    String annotationFilter = info.getResourceMethod()
        .getAnnotation(PageReplyFilter.class).filter();
    String[] finalFilter;
    if (res.getEntity().getClass() == Page.class) {
      finalFilter = DEFAULT_PAGE_FILTER;
    } else {
      finalFilter = DEFAULT_OBJECT_FILTER;
    }

    // Add additional elements to the filter as presented in the annotation.
    if (StringUtils.isNotBlank(annotationFilter)) {
      finalFilter = ArrayUtils.addAll(finalFilter, annotationFilter.split(","));
    }
    log.trace("Filtering '{}' with '{}'.", res.getEntity().getClass(),
        finalFilter);

    // Perform the filtering and mapping.
    clonedMapper.addMixIn(Object.class, AntPathFilterMixin.class);
    clonedMapper.setFilterProvider(new SimpleFilterProvider().addFilter(
        "antPathFilter", new AntPathPropertyFilter(finalFilter)));
    String result = clonedMapper.writeValueAsString(res.getEntity());

    log.trace("Filtering result is '{}'.", result);

    res.setEntity(result);
  }
}
