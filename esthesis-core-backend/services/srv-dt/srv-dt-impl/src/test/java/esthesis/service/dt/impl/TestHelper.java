package esthesis.service.dt.impl;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.core.common.entity.CommandReplyEntity;
import jakarta.enterprise.context.ApplicationScoped;
import org.instancio.Instancio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

/**
 * Utility class for creating test data in the device template (DT) service tests.
 */
@ApplicationScoped
public class TestHelper {


	public List<CommandReplyEntity> makeReplies(int numReplies) {
		List<CommandReplyEntity> replies = new ArrayList<>();
		for (int i = 0; i < numReplies; i++) {
			replies.add(makeReply());
		}
		return replies;
	}

	private CommandReplyEntity makeReply() {
		return Instancio.of(CommandReplyEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(field(CommandReplyEntity.class, "createdOn"), Instant.now().minus(1, ChronoUnit.MINUTES))
			.create();
	}

	public ExecuteRequestScheduleInfoDTO makeExecuteRequestScheduleInfo() {
		return Instancio.of(ExecuteRequestScheduleInfoDTO.class)
			.set(field(ExecuteRequestScheduleInfoDTO.class, "devicesMatched"), 1)
			.set(field(ExecuteRequestScheduleInfoDTO.class, "devicesScheduled"), 1)
			.create();
	}
}
