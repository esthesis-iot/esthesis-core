package esthesis.service.common.paging;

import java.util.List;
import lombok.Data;

@Data
public class Page<D> {

  public int page;
  public int size;
  public long totalElements;
  public List<D> content;
}
