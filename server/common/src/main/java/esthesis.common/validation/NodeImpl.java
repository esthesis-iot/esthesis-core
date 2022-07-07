package esthesis.common.validation;

import javax.validation.ElementKind;
import javax.validation.Path.Node;
import lombok.Data;

@Data
public class NodeImpl implements Node {

  private String name;
  private boolean inIterable;
  private Integer index;
  private Object key;
  private ElementKind kind;

  @Override
  public <T extends Node> T as(Class<T> nodeType) {
    return null;
  }
}
