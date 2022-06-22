package esthesis.services.confpublic;

import esthesis.dto.PublicConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/v1/conf-public")
public class PublicConfiguration {

  @GET
  public PublicConfig getPublicConfig() {
    PublicConfig publicConfig = new PublicConfig();
    publicConfig.setOidcUrl("http://192.168.21.2");

    return publicConfig;
  }

}
