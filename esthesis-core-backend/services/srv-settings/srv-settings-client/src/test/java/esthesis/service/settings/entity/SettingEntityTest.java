package esthesis.service.settings.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class SettingEntityTest {

	@Test
	void asString() {
		SettingEntity setting = new SettingEntity("testString", "testValue");
		assertEquals("testValue", setting.asString());
	}

	@Test
	void asInt() {
		SettingEntity setting = new SettingEntity("testInt", "123");
		assertEquals(123, setting.asInt());
	}

	@Test
	void asLong() {
		SettingEntity setting = new SettingEntity("testLong", "123456789");
		assertEquals(123456789L, setting.asLong());
	}

	@Test
	void asBoolean() {
		SettingEntity settingTrue = new SettingEntity("testBooleanTrue", "true");
		assertTrue(settingTrue.asBoolean());

		SettingEntity settingFalse = new SettingEntity("testBooleanFalse", "false");
		assertFalse(settingFalse.asBoolean());
	}

	@Test
	void asObjectId() {
		SettingEntity setting = new SettingEntity("testObjectId", "507f1f77bcf86cd799439011");
		assertEquals("507f1f77bcf86cd799439011", setting.asObjectId().toHexString());
	}
}
