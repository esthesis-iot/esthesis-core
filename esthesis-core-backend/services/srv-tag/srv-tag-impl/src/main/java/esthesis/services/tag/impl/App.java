package esthesis.services.tag.impl;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A component providing access to tag services.
 */
@ApplicationScoped
public class App {

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Inject
	MongoClient mongoClient;

	@ConfigProperty(name = "quarkus.mongodb.database")
	String dbName;

	@SuppressWarnings("unused")
	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner(appName);

		// Create MongoDB indexes.
		MongoDatabase db = mongoClient.getDatabase(dbName);
		db.getCollection("Tag").createIndex(Indexes.ascending("name"),
			new IndexOptions().name("idxName").unique(true));
	}
}
