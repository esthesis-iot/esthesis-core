package esthesis.service.common.gridfs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import esthesis.common.exception.QDoesNotExistException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Service for interacting with a GridFS bucket.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class GridFSService {
	private static final String GRIDFS_METADATA_PREFIX = "metadata.";

	@Inject
	MongoClient mongoClient;

	/**
	 * Saves a binary file to the GridFS bucket.
	 *
	 * @param gridFSDTO the GridFS DTO containing the file, metadata, and bucket name.
	 * @return the ObjectId of the saved file.
	 * @throws IOException if the file cannot be read.
	 */
	public ObjectId saveBinary(@Valid GridFSDTO gridFSDTO)
	throws IOException {
		MongoDatabase database = mongoClient.getDatabase(gridFSDTO.getDatabase());
		GridFSUploadOptions options = new GridFSUploadOptions()
			.chunkSizeBytes(1048576)
			.metadata(new Document(gridFSDTO.getMetadataName(), gridFSDTO.getMetadataValue()));
		GridFSBucket gridFSBucket = GridFSBuckets.create(database, gridFSDTO.getBucketName());
		try (InputStream streamToUploadFrom =
			new FileInputStream(gridFSDTO.getFile().uploadedFile().toFile())) {
			return gridFSBucket.uploadFromStream(gridFSDTO.getFile().fileName(), streamToUploadFrom, options);
		}
	}

	/**
	 * Deletes a binary file from the GridFS bucket.
	 *
	 * @param gridFSDTO the GridFS DTO containing the metadata and bucket name.
	 */
	public void deleteBinary(@Valid GridFSDTO gridFSDTO) {
		MongoDatabase database = mongoClient.getDatabase(gridFSDTO.getDatabase());
		GridFSBucket gridFSBucket = GridFSBuckets.create(database, gridFSDTO.getBucketName());
		gridFSBucket.find(new Document(GRIDFS_METADATA_PREFIX + gridFSDTO.getMetadataName(),
				gridFSDTO.getMetadataValue()))
			.forEach(gridFSFile -> gridFSBucket.delete(gridFSFile.getObjectId()));
	}

	/**
	 * Downloads a binary file from the GridFS bucket.
	 *
	 * @param gridFSDTO the GridFS DTO containing the metadata and bucket name.
	 * @return the binary file as a byte array.
	 */
	public Uni<byte[]> downloadBinary(@Valid GridFSDTO gridFSDTO) {
		MongoDatabase database = mongoClient.getDatabase(gridFSDTO.getDatabase());
		GridFSBucket gridFSBucket = GridFSBuckets.create(database, gridFSDTO.getBucketName());

		return Uni.createFrom().emitter(em -> {
			var cursor = gridFSBucket.find(new Document(GRIDFS_METADATA_PREFIX + gridFSDTO.getMetadataName(),
					gridFSDTO.getMetadataValue()));
			try (MongoCursor<GridFSFile> iterator = cursor.iterator()) {
				if (iterator.hasNext()) {
					cursor.forEach(gridFSFile -> {
						try {
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							gridFSBucket.downloadToStream(gridFSFile.getFilename(), outputStream);
							em.complete(outputStream.toByteArray());
						} catch (Exception e) {
							em.fail(e);
						}
					});
				} else {
					em.fail(new QDoesNotExistException("File not found for the given metadata name '{}' and "
						+ "value '{}'.", gridFSDTO.getMetadataName(), gridFSDTO.getMetadataValue()));
				}
			}
		});
	}
}
