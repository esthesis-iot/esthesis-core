package esthesis.services.settings.impl.service;

import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.services.settings.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DevicePageFieldServiceTest {

	@Inject
	DevicePageFieldService devicePageFieldService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void saveFields() {

		// Perform a save operation for a new field.
		devicePageFieldService.saveFields(
			List.of(
				new DevicePageFieldEntity("test-measurement",
					true,
					"test-label",
					"test-formatter",
					"test-icon")));

		// Assert the field was saved with the correct values.
		DevicePageFieldEntity field = devicePageFieldService.getFields().getFirst();
		assertEquals("test-measurement", field.getMeasurement());
		assertTrue(field.isShown());
		assertEquals("test-label", field.getLabel());
		assertEquals("test-formatter", field.getFormatter());
	}

	@Test
	void getFields() {
		// Assert no fields exist.
		assertTrue(devicePageFieldService.getFields().isEmpty());

		// Perform a save operation for a new field.
		devicePageFieldService.saveFields(List.of(testHelper.makeDevicePageFieldEntity("test-measurement")));

		// Assert fields exist.
		assertFalse(devicePageFieldService.getFields().isEmpty());
	}
}
