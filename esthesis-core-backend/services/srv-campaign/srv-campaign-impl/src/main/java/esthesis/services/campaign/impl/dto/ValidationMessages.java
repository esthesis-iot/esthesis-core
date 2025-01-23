package esthesis.services.campaign.impl.dto;

/**
 * A collection of validation messages for error handling.
 */
public class ValidationMessages {

	private ValidationMessages() {
	}

	public static final String POSITIVE_INTEGER = "Must be a positive integer";
	public static final String STAGE_REQUIRED = "Stage is required";
	public static final String OPERATION_REQUIRED = "Operation is required";
	public static final String DATE_REQUIRED = "Date is required";
	public static final String DATE_IN_PAST = "Date cannot be in the past";
	public static final String GENERIC = "Value is required";
	public static final String PROPERTY_NAME_REQUIRED = "Property name is required";
}
