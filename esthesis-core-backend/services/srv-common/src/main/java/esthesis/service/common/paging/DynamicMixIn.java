package esthesis.service.common.paging;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * MixIn class to dynamically filter fields in response.
 */
@JsonFilter("dynamicFilter")
public class DynamicMixIn {

}
