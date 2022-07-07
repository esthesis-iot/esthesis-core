package esthesis.common.dto;

import java.io.Serializable;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public abstract class BaseDTO implements Serializable {

  private ObjectId id;
}
