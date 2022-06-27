package esthesis.common.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public abstract class BaseDTO {

  private ObjectId id;
}
