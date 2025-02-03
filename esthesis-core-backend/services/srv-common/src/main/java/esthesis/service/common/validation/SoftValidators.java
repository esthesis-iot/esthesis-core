package esthesis.service.common.validation;

/**
 * Custom validators that do not throw exceptions, but return boolean values.
 */
public class SoftValidators {

	private SoftValidators() {
	}

	public static boolean isPositiveInteger(String value) {
		try {
			int intValue = Integer.parseInt(value);
			return intValue > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isNotPositiveInteger(String value) {
		return !isPositiveInteger(value);
	}
}
