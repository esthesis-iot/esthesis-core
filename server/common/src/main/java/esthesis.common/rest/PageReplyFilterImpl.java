package esthesis.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import java.text.MessageFormat;
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
  ObjectMapper mapper;

  private static final String DEFAULT_PAGE_FILTER =
      "page,size,totalElements,content[{0}]";
  private static final String DEFAULT_OBJECT_FILTER = "{}";

  @Override
  public void filter(ContainerRequestContext req,
      ContainerResponseContext res) {

    String annotationFilter = info.getResourceMethod()
        .getAnnotation(PageReplyFilter.class).filter();

    String filterTemplate;
    if (res.getEntity().getClass() == Page.class) {
      filterTemplate = DEFAULT_PAGE_FILTER;
    } else {
      filterTemplate = DEFAULT_OBJECT_FILTER;
    }

    if (StringUtils.isBlank(annotationFilter)) {
      annotationFilter = MessageFormat.format(filterTemplate, "*");
    } else {
      annotationFilter = MessageFormat.format(filterTemplate, annotationFilter);
    }
    log.debug("Filtering {} with {}.", res.getEntity().getClass(),
        annotationFilter);

    res.setEntity(
        SquigglyUtils.stringify(Squiggly.init(mapper, annotationFilter),
            res.getEntity()));
  }
}
