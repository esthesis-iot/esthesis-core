package esthesis.service.common.logging;

import lombok.Data;

/**
 * A representation of a custom error reply.
 */
@Data
public class QErrorReply {

	private String errorMessage;
	private String traceId;
}
