package esthesis.service.provisioning.impl.gridfs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class GridFSDBMigration {
	@Inject
	MongoClient mongoClient;

	@ConfigProperty(name = "quarkus.mongodb.database")
	String dbName;

	private final static String GRIDFS_BUCKET_NAME = "ProvisioningPackageBucket";

	public void checkMigration() {
		log.debug("Creating GridFS bucket '{}' in database '{}'", GRIDFS_BUCKET_NAME, dbName);
		MongoDatabase database = mongoClient.getDatabase(dbName);
		GridFSBucket gridFSBucket = GridFSBuckets.create(database, GRIDFS_BUCKET_NAME);
		log.debug("GridFS bucket '{}' created successfully", gridFSBucket.getBucketName());
	}
}
