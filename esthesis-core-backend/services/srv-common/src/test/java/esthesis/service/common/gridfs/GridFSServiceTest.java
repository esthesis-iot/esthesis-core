package esthesis.service.common.gridfs;

import esthesis.common.exception.QDoesNotExistException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class GridFSServiceTest {

	@Inject
	GridFSService gridFSService;

	// Temporary file used for testing.
	File testFile;

	@AfterEach
	void cleanup() {
		if (testFile != null && testFile.exists()) {
			testFile.delete();
		}
	}

	// Creates a GridFSDTO with a temporary file and random metadata.
	private GridFSDTO createTestDTO() throws IOException {
		Path tempDir = Files.createTempDirectory("gridfs-test-");
		testFile = tempDir.resolve("testfile.txt").toFile();

		try (FileOutputStream out = new FileOutputStream(testFile)) {
			out.write("hello world".getBytes(StandardCharsets.UTF_8));
		}

		return new GridFSDTO("ref", UUID.randomUUID().toString(), "testbucket", "testdb", new MockFileUpload(testFile));
	}

	@Test
	void testSaveDownloadAndDelete() throws IOException {
		GridFSDTO dto = createTestDTO();

		// Perform save operation and assert the ObjectId is not null.
		ObjectId objectId = gridFSService.saveBinary(dto);
		assertNotNull(objectId);

		// Download the binary data and assert its content.
		byte[] data = gridFSService.downloadBinary(dto).await().indefinitely();
		assertNotNull(data);
		assertEquals("hello world", new String(data, StandardCharsets.UTF_8));


		// Delete the binary file and assert it no longer exists.
		gridFSService.deleteBinary(dto);

		assertThrows(QDoesNotExistException.class, () -> gridFSService.downloadBinary(dto).await().indefinitely());

	}


	// Mock implementation of FileUpload for testing purposes.
	static class MockFileUpload implements FileUpload {
		private final File file;

		MockFileUpload(File file) {
			this.file = file;
		}

		@Override
		public Path filePath() {
			return file.toPath();
		}

		@Override
		public String name() {
			return "file";
		}

		@Override
		public String fileName() {
			return file.getName();
		}

		@Override
		public long size() {
			return file.length();
		}

		@Override
		public String contentType() {
			return "text/plain";
		}

		@Override
		public String charSet() {
			return StandardCharsets.UTF_8.name();
		}
	}
}
