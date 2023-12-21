package esthesis.service.common.validation;

import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class PathImpl implements Path {

	private final String path;

	public PathImpl(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public Iterator<Node> iterator() {
		List<Node> list = new ArrayList<>();
		list.add(new NodeImpl());
		return list.iterator();
	}

	@Override
	public void forEach(Consumer<? super Node> action) {
		Path.super.forEach(action);
	}

	@Override
	public Spliterator<Node> spliterator() {
		return Path.super.spliterator();
	}
}
