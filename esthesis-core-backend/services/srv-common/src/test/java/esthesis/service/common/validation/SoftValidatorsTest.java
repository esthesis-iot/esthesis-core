package esthesis.service.common.validation;

import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertTrue;

import org.junit.jupiter.api.Test;

class SoftValidatorsTest {

	@Test
	void testIsPositiveInteger() {
		assertTrue(SoftValidators.isPositiveInteger("1"));
		assertFalse(SoftValidators.isPositiveInteger("0"));
		assertFalse(SoftValidators.isPositiveInteger("-1"));
		assertFalse(SoftValidators.isPositiveInteger("abc"));
		assertFalse(SoftValidators.isPositiveInteger("1.0"));
		assertFalse(SoftValidators.isPositiveInteger("1,0"));
		assertFalse(SoftValidators.isPositiveInteger("1.0.0"));
		assertFalse(SoftValidators.isPositiveInteger("1,0,0"));
		assertFalse(SoftValidators.isPositiveInteger("1.0a"));
		assertFalse(SoftValidators.isPositiveInteger(""));
		assertFalse(SoftValidators.isPositiveInteger(null));
	}
}
