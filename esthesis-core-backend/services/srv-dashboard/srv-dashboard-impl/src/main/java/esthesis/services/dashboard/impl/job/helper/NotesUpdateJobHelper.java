package esthesis.services.dashboard.impl.job.helper;

import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.config.DashboardItemNotesConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateNotes;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateNotes.DashboardUpdateNotesBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class NotesUpdateJobHelper extends UpdateJobHelper<DashboardUpdateNotes> {

	public DashboardUpdateNotes refresh(DashboardEntity dashboardEntity,
		DashboardItemDTO item) {
		DashboardUpdateNotesBuilder<?, ?> replyBuilder = DashboardUpdateNotes.builder()
			.id(item.getId())
			.type(Type.NOTES);

		try {
			// Get item configuration and return notes.
			DashboardItemNotesConfiguration config = getConfig(DashboardItemNotesConfiguration.class,
				item);

			return replyBuilder.notes(config.getNotes()).build();
		} catch (Exception e) {
			log.error("Error processing '{}' for dashboard item '{}'.", Type.NOTES, item.getId(), e);
			return replyBuilder.isError(true).build();
		}
	}

}
