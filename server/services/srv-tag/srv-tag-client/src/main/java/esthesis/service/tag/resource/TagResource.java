package esthesis.service.tag.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "TagResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface TagResource {

  @GET
  @Path("/v1/tag/find")
  Page<TagEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/tag/get-all")
  List<TagEntity> getAll();

  /**
   * Finds a tag by its name.
   *
   * @param name         The name of the tag to search.
   * @param partialMatch If true, the search will be performed using a partial match.
   */
  @GET
  @Path("/v1/tag/find/by-name/{name}")
  TagEntity findByName(@PathParam("name") String name,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  /**
   * Finds multiple tags by their names.
   *
   * @param name         A comma-separated list of names.
   * @param partialMatch If true, the search will be performed using a partial match.
   */
  @GET
  @Path("/v1/tag/find/by-names")
  List<TagEntity> findByNames(@QueryParam("names") String name,
      @QueryParam("partialMatch") @DefaultValue("false") boolean partialMatch);

  @GET
  @Path("/v1/tag/{id}")
  TagEntity findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/tag/{id}")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/tag")
  @Produces("application/json")
  TagEntity save(@Valid TagEntity tagEntity);
}
