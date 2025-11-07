package esthesis.services.tag.impl.repository;

import esthesis.service.tag.entity.TagEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for TagRepository, testing tag repository functionality.
 */
@QuarkusTest
class TagRepositoryTest {

	@Inject
	TagRepository tagRepository;

	@BeforeEach
	void setUp() {
		tagRepository.deleteAll();
	}

	@Test
	void findByName() {
		assertTrue(tagRepository.findByName("test").isEmpty());
		assertTrue(tagRepository.findByName(List.of("test")).isEmpty());

		tagRepository.persist(new TagEntity("test", "Test Tag"));

		assertFalse(tagRepository.findByName("test").isEmpty());
		assertFalse(tagRepository.findByName(List.of("test")).isEmpty());
	}





}
