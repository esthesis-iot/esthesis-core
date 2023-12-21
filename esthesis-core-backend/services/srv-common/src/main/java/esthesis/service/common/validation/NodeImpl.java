package esthesis.service.common.validation;

import jakarta.validation.ElementKind;
import jakarta.validation.Path.Node;
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
