package esthesis.dataflows.oriongateway.dto;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrionQueryDTO implements Serializable {

	//Comma separated list of URIs to be retrieved
	@QueryParam("id")
	private String id;

	//Regular expression that must be matched by Entity ids
	@QueryParam("idPattern")
	private String idPattern;

	//Comma separated list of Entity type names to be retrieved
	@QueryParam("type")
	private String type;

	//Custom queries E.g. by attribute value using the following format: q=?attribute==value
	@QueryParam("q")
	private String q;

	//Comma separated list of attribute names (properties or relationships) to be retrieved
	@QueryParam("attrs")
	private String attrs;

	//Pagination limit
	@QueryParam("limit")
	private Integer limit;
}
