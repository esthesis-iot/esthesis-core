package esthesis.common.dto;

import java.io.Serializable;
import lombok.Data;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestForm;

@Data
public abstract class BaseDTO implements Serializable {

  @RestForm
  private ObjectId id;
}
