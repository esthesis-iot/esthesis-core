package esthesis.service.common.validation;

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
