package esthesis.services.tag.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.service.tag.entity.TagEntity;
import esthesis.services.tag.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TagServiceTest {

	@Inject
	TagService tagService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void find() {
		// Assert no tags exist.
		assertTrue(tagService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());

		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tags exist.
		assertFalse(tagService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());
	}

	@Test
	void findPartial() {
		// Assert no tags exist.
		assertTrue(tagService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());

		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tags exist.
		assertFalse(tagService.find(testHelper.makePageable(0, 100)).getContent().isEmpty());
	}

	@Test
	void saveNew() {
		// Perform a save operation for a new tag.
		String tagId =
			tagService.saveNew(new TagEntity("test-tag", "test description")).getId().toHexString();

		// Assert tag was saved with the correct values.
		TagEntity tag = tagService.findById(tagId);
		assertEquals("test-tag", tag.getName());
		assertEquals("test description", tag.getDescription());
	}

	@Test
	void saveUpdate() {
		// Perform a save operation for a new tag.
		String tagId =
			tagService.saveNew(new TagEntity("test-tag", "test description")).getId().toHexString();

		// Find tag and perform an update.
		TagEntity tag = tagService.findById(tagId);
		tag.setName("test-tag-updated");
		tag.setDescription("test description updated");
		tagService.saveUpdate(tag);

		// Assert tag was updated with the correct values.
		tag = tagService.findById(tagId);
		assertEquals("test-tag-updated", tag.getName());
		assertEquals("test description updated", tag.getDescription());
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new tag.
		String tagId =
			tagService.saveNew(new TagEntity("test-tag", "test description")).getId().toHexString();

		// Assert tag can be found.
		assertNotNull(tagService.findById(tagId));

		// Perform a delete operation for the given tag ID.
		tagService.deleteById(tagId);

		// Assert tag cannot be found.
		assertNull(tagService.findById(tagId));
	}

	@Test
	void findByName() {
		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tag can be found by name and partial name.
		assertFalse(tagService.findByName("test-tag").isEmpty());

		// Assert non-existent tag cannot be found by name.
		assertTrue(tagService.findByName("non-existent-tag").isEmpty());
	}

	@Test
	void findByNames() {
		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tag can be found by name and partial name.
		assertFalse(tagService.findByName(List.of("test-tag")).isEmpty());

		// Assert non-existent tag cannot be found by name.
		assertTrue(tagService.findByName(List.of("non-existent-tag")).isEmpty());
	}

	@Test
	void getAll() {
		// Assert no tags exist.
		assertTrue(tagService.getAll().isEmpty());

		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tags exist.
		assertFalse(tagService.getAll().isEmpty());
	}

	@Test
	void findById() {
		// Perform a save operation for a new tag.
		String tagId =
			tagService.saveNew(new TagEntity("test-tag", "test description")).getId().toHexString();

		// Assert tag can be found.
		assertNotNull(tagService.findById(tagId));
	}

	@Test
	void findByColumn() {
		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tag can be found.
		assertFalse(tagService.findByColumn("name", "test-tag").isEmpty());
		assertFalse(tagService.findByColumn("description", "test description").isEmpty());

	}

	@Test
	void findByColumnIn() {
		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tag can be found.
		assertFalse(tagService.findByColumnIn("name", List.of("test-tag")).isEmpty());
		assertFalse(
			tagService.findByColumnIn("description", List.of("test description")).isEmpty());
	}

	@Test
	void findFirstByColumn() {
		// Perform a save operation for a new tag.
		tagService.saveNew(new TagEntity("test-tag", "test description"));

		// Assert tag can be found.
		assertNotNull(tagService.findFirstByColumn("name", "test-tag"));
		assertNotNull(tagService.findFirstByColumn("description", "test description"));
	}
}
