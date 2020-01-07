package com.bpmid.matchers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonPattern {
	private HashMap<String, PropertyDescription> properties = new HashMap<String, PropertyDescription>();
	private JsonPatternCompareMode compareMode;
	private String diffDescription;
	private boolean isDebugEnabled = false;
	
	public JsonPattern() {
		this.compareMode = JsonPatternCompareMode.STRICT;
	}
	public JsonPattern(JsonPatternCompareMode compareMode) {
		this.compareMode = compareMode;
	}

	public JsonPattern enableDebugLogging() {
		this.isDebugEnabled = true;
		return this;
	}
	
	public JsonPattern addMandatoryProperty(String name, String value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.MANDATORY, value));
		return this;
	}

	public JsonPattern addOptionalProperty(String name, String value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.OPTIONAL, value));
		return this;
	}

	public JsonPattern addMandatoryProperty(String name, Matcher<String> matcher) {
		properties.put(name,  new PropertyDescription(PropertyConstraint.MANDATORY, matcher));
		return this;
	}
	public JsonPattern addOptionalProperty(String name, Matcher<String> matcher) {
		properties.put(name,  new PropertyDescription(PropertyConstraint.OPTIONAL, matcher));
		return this;
	}

	private class PropertyDescription {
		private PropertyConstraint constraint;
		private Matcher<String> valueMatcher;
		private String expectedValue;
		
		PropertyDescription (PropertyConstraint constraint, String value) {
			this.constraint = constraint;
			this.expectedValue = value;
		}


		public PropertyDescription(PropertyConstraint constraint, Matcher<String> valueMatcher) {
			this.constraint = constraint;
			this.valueMatcher = valueMatcher;
		}

		public boolean checkValue(String value) {
			if (value == null) {
				return (expectedValue == null);
			} else {
				if (valueMatcher != null)
					return valueMatcher.matches(value);
				else {
					if (expectedValue == null)
						return false;
					else 
						return expectedValue.equals(value);
				}
					
			}
			
		}

		public boolean isOptional() {
			if (this.constraint == null || this.constraint.equals(PropertyConstraint.OPTIONAL))
				return true;
			else return false;
		}
	}
	
	private enum PropertyConstraint {
		MANDATORY,
		OPTIONAL
	}
	public String toDescription() {
		StringBuilder sb = new StringBuilder("\n{\n");
		for (String fieldName : this.properties.keySet()) {
			PropertyDescription p = properties.get(fieldName);
			if (p.valueMatcher == null)
				sb.append(String.format("  %s (%s) : %s\n", fieldName, p.constraint, p.expectedValue));
			else 
				sb.append(String.format("  %s (%s) : %s\n", fieldName, p.constraint, p.valueMatcher.toString()));
		}
		sb.append("}\n");
		return sb.toString();
	}

	public boolean isSimilarTo(JsonNode actualJson) {
		if (!_allMandatoryFieldsPresentedIn(actualJson))
			return false;
			
		if (!_allPresentedFieldsHasValuesAsExpected(actualJson))
			return false;
		
		diffDescription = null;
		return true;
	}

	
	private boolean _allPresentedFieldsHasValuesAsExpected(JsonNode actualJson) {
		if (isDebugEnabled)
			System.out.println("Checking if all presented fields has values as expected:");
		Iterator<Entry<String, JsonNode>> i = actualJson.fields();
		while(i.hasNext()) {
			Entry<String, JsonNode> entry = i.next();
			if (isDebugEnabled)
				System.out.print(String.format("  %s : ",  entry.getKey()));
			
			if (properties.containsKey(entry.getKey())) {
				PropertyDescription p = properties.get(entry.getKey());
				if (!p.checkValue(entry.getValue().asText())) {
					if (isDebugEnabled)
						System.out.println(String.format("%s -> ERROR (wrong value)", entry.getValue().asText()));
					
					diffDescription = String.format("Value of field '%s' does not match what expected", entry.getKey());
					return false;
				} else {
					if (isDebugEnabled)
						System.out.println(String.format("%s -> OK", entry.getValue().asText()));
				}
			} else {
				if (!compareMode.equals(JsonPatternCompareMode.ADDITIONAL_FIELDS_ALLOWED)) {
					if (isDebugEnabled)
						System.out.println("ERROR (field is not expected)");
					
					diffDescription = String.format("Field '%s' is not expected!", entry.getKey());
					return false;
				} else {
					if (isDebugEnabled)
						System.out.println(String.format("%s -> OK (skipped)", entry.getValue().asText()));
				}
			}
		}
		if (isDebugEnabled)
			System.out.println("All presented fields are OK");
		return true;
	}

	private boolean _allMandatoryFieldsPresentedIn(JsonNode actualJson) {
		
		if (isDebugEnabled)
			System.out.println("Checking if all mandatoryFieldsPresented");
		for (String fieldName : this.properties.keySet()) {
			if (isDebugEnabled)
				System.out.print(String.format("  %s : ",  fieldName));
			
			PropertyDescription p = properties.get(fieldName);
			if (p.isOptional()) {
				if (isDebugEnabled)
					System.out.println("optional, skiping");
				continue;
			}
			JsonNode value = actualJson.get(fieldName);
			if (value == null) {
				if (isDebugEnabled)
					System.out.println("NOT FOUND, ERROR!");
				this.diffDescription = String.format("MANDATORY field '%s' not found in actual JSON", fieldName);
				return false;
			} else {
				if (isDebugEnabled)
					System.out.println("FOUND, OK");
			}
		}
		if (isDebugEnabled)
			System.out.println("All mandatory Fields Presented");
		
		return true;
	}
	public String getDiffDescription() {
		return diffDescription;
	}

}
