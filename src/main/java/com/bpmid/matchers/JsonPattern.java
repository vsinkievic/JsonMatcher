package com.bpmid.matchers;

import java.math.BigDecimal;
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

	public JsonPattern addMandatoryProperty(String name, Boolean value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.MANDATORY, value));
		return this;
	}

	public JsonPattern addOptionalProperty(String name, Boolean value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.OPTIONAL, value));
		return this;
	}

	public JsonPattern addMandatoryProperty(String name, BigDecimal value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.MANDATORY, value));
		return this;
	}

	public JsonPattern addOptionalProperty(String name, BigDecimal value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.OPTIONAL, value));
		return this;
	}

	public JsonPattern addMandatoryProperty(String name, Integer value) {
		properties.put(name, new PropertyDescription(PropertyConstraint.MANDATORY, value));
		return this;
	}

	public JsonPattern addOptionalProperty(String name, Integer value) {
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
		private ValueType valueType;
		private String expectedString;
		private Boolean expectedBool;
		private Integer expectedInt;
		private BigDecimal expectedDecimal;
		
		PropertyDescription (PropertyConstraint constraint, String value) {
			this.constraint = constraint;
			this.valueType = ValueType.STRING;
			this.expectedString = value;
		}
		
		PropertyDescription (PropertyConstraint constraint, Boolean value) {
			this.constraint = constraint;
			this.valueType = ValueType.BOOLEAN;
			this.expectedBool = value;
		}
		
		PropertyDescription (PropertyConstraint constraint, Integer value) {
			this.constraint = constraint;
			this.valueType = ValueType.INTEGER;
			this.expectedInt = value;
		}
		
		PropertyDescription (PropertyConstraint constraint, BigDecimal value) {
			this.constraint = constraint;
			this.valueType = ValueType.BIGDECIMAL;
			this.expectedDecimal = value;
		}

		public PropertyDescription(PropertyConstraint constraint, Matcher<String> valueMatcher) {
			this.constraint = constraint;
			this.valueType = ValueType.MATCHER;
			this.valueMatcher = valueMatcher;
		}

		public boolean checkValue(JsonNode valueNode) {
			
			if (valueType.equals(ValueType.MATCHER)) {
				return valueMatcher.matches(valueNode.asText());
			} else if (valueType.equals(ValueType.STRING)){
				if (valueNode.isNull()) 
					return (expectedString == null);
				else {
					if (expectedString == null)
						return false;
					else 
						return expectedString.equals(valueNode.asText());
				}
			} else if (valueType.equals(ValueType.BOOLEAN)) {
				if (valueNode.isNull()) 
					return (expectedBool == null);
				else {
					if (expectedBool == null)
						return false;
					else 
						return expectedBool.equals(valueNode.asBoolean());
				}
			} else if (valueType.equals(ValueType.INTEGER)) {
				if (valueNode.isNull()) 
					return (expectedBool == null);
				else {
					if (expectedInt == null)
						return false;
					else 
						return expectedInt.equals(valueNode.asInt());
				}
			} else if (valueType.equals(ValueType.BIGDECIMAL)) {
				if (valueNode.isNull()) 
					return (expectedBool == null);
				else {
					if (expectedDecimal == null)
						return false;
					else 
						return expectedDecimal.equals(valueNode.decimalValue());
				}
			} else 
				return false;
			
		}

		public boolean isOptional() {
			if (this.constraint == null || this.constraint.equals(PropertyConstraint.OPTIONAL))
				return true;
			else return false;
		}

		public String getExpectedValueAsText() {
			if (valueType.equals(ValueType.MATCHER)) {
				return valueMatcher == null ? "null" : valueMatcher.toString();
			} else if (valueType.equals(ValueType.STRING)){
				return expectedString == null ? "null" : expectedString;
			} else if (valueType.equals(ValueType.BOOLEAN)) {
				return Boolean.toString(expectedBool);
			} else if (valueType.equals(ValueType.INTEGER)) {
				return Integer.toString(expectedInt);
			} else if (valueType.equals(ValueType.BIGDECIMAL)) {
				return expectedDecimal == null ? "null" : expectedDecimal.toString();
			} else 
				throw new RuntimeException(String.format("Not supported valueType=%s", valueType.toString()));
		}
	}
	
	private enum PropertyConstraint {
		MANDATORY,
		OPTIONAL
	}
	enum ValueType {
		STRING,
		BOOLEAN,
		INTEGER,
		BIGDECIMAL,
		MATCHER
	}
	public String toDescription() {
		StringBuilder sb = new StringBuilder("\n{\n");
		for (String fieldName : this.properties.keySet()) {
			PropertyDescription p = properties.get(fieldName);
			if (p.valueMatcher == null)
				sb.append(String.format("  %s (%s) : %s\n", fieldName, p.constraint, p.expectedString));
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
			System.out.println("Checking if all presented fields have values as expected:");
		Iterator<Entry<String, JsonNode>> i = actualJson.fields();
		while(i.hasNext()) {
			Entry<String, JsonNode> entry = i.next();
			if (isDebugEnabled)
				System.out.print(String.format("  %s : ",  entry.getKey()));
			
			if (properties.containsKey(entry.getKey())) {
				PropertyDescription p = properties.get(entry.getKey());
				if (!p.checkValue(entry.getValue())) {
					if (isDebugEnabled)
						System.out.println(String.format("'%s' -> ERROR (wrong value, expected '%s')", entry.getValue().asText(), p.getExpectedValueAsText()));
					
					diffDescription = String.format("Value of field '%s' does not match what expected", entry.getKey());
					return false;
				} else {
					if (isDebugEnabled)
						System.out.println(String.format("'%s' -> OK (%s)", entry.getValue().asText(), p.getExpectedValueAsText()));
				}
			} else {
				if (!compareMode.equals(JsonPatternCompareMode.ADDITIONAL_FIELDS_ALLOWED)) {
					if (isDebugEnabled)
						System.out.println(String.format("ERROR, field is not expected, value='%s'", entry.getValue().asText()));
					
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
