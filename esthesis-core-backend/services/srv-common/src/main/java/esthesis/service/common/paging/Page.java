package esthesis.service.common.paging;

import java.util.List;
import lombok.Data;

@Data
public class Page<D> {

	private int index;
	private int size;
	private long totalElements;
	private List<D> content;
}
