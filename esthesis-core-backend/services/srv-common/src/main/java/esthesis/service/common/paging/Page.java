package esthesis.service.common.paging;

import java.util.List;
import lombok.Data;

/**
 * Support for paging parameters.
 *
 * @param <D>
 */
@Data
public class Page<D> {

	private int index;
	private int size;
	private long totalElements;
	private List<D> content;
}
