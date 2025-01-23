package esthesis.service.dataflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A representation of a select option for Formly selects.
 */
@Data
@AllArgsConstructor
public class FormlySelectOption {

	private String label;
	private String value;
}
