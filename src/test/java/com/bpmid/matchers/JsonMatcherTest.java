package com.bpmid.matchers;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

import org.hamcrest.Matchers;

import static com.bpmid.matchers.JsonMatcher.matchesJson;

import org.junit.Test;

public class JsonMatcherTest {

	@Test
	public void testJsonContainsFieldNotListedInPatternInStrictCompareMode() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\" }";
		
		// then
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1"))));
		assertThat(json, not(matchesJson(new JsonPattern(JsonPatternCompareMode.STRICT).addMandatoryProperty("field1", "value1"))));
	}

	@Test
	public void testJsonContainsFieldNotListedInPatternInWeakCompareMode() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\" }";
		
		// then
		assertThat(json, JsonMatcher.matchesJson(new JsonPattern(JsonPatternCompareMode.ADDITIONAL_FIELDS_ALLOWED).addMandatoryProperty("field1", "value1")));
	}

	@Test
	public void testMandatoryFieldsExistsWithSameValues() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field3\": \"value3\" }";
		
		// then
		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                      .addMandatoryProperty("field2", "value2")
				                                      .addMandatoryProperty("field3", "value3")));
	}

	@Test
	public void testMandatoryFieldExistsAndMatchesValues() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field3\": \"value3\" }";
		
		// then
		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                      .addMandatoryProperty("field2", Matchers.startsWith("value"))
				                                      .addMandatoryProperty("field3", "value3")));
	}

	@Test
	public void testMandatoryFieldExistsAndDoesntMatchValues() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"valu2\", \"field3\": \"value3\" }";

		// then
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                          .addMandatoryProperty("field2", Matchers.startsWith("value"))
				                                          .addMandatoryProperty("field3", "value3"))));
	}

	@Test
	public void testMandatoryFieldHasDifferentValue() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"different\", \"field3\": \"value3\" }";
		
		// then
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                          .addMandatoryProperty("field2", "value2")
				                                          .addMandatoryProperty("field3", "value3"))));
	}

	@Test
	public void testMandatoryFieldsWithoutValues_fieldNotExists() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field3\": null }";
		
		// then
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                          .addMandatoryProperty("field2", "value2")
				                                          .addMandatoryProperty("field3", "value3")
				                                          .addMandatoryProperty("field4", "value4"))));
	}

	@Test
	public void testOptionalField_fieldNotExists() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field3\": \"value3\" }";
		
		// then
		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                      .addMandatoryProperty("field2", "value2")
				                                      .addMandatoryProperty("field3", "value3")
				                                      .addOptionalProperty("field4", "value4")));
	}

	@Test
	public void testOptionalField_fieldExistsWithExactValue() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field4\" : \"value4\" }";
		
		// then
		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
				                                      .addMandatoryProperty("field2", "value2")
				                                      .addOptionalProperty("field4", "value4")));

		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
									                .addMandatoryProperty("field2", "value2")
									                .addOptionalProperty("field4", Matchers.startsWith("value"))));

		assertThat(json, matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
									                .addMandatoryProperty("field2", "value2")
									                .addOptionalProperty("field4", Matchers.any(String.class))));

	}

	@Test
	public void testOptionalField_fieldExistsWithDifferentValue() {
		// given
		String json = "{ \"field1\": \"value1\", \"field2\":\"value2\", \"field3\": null, \"field4\" : \"different\" }";
		
		// then
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
										                .addMandatoryProperty("field2", "value2")
										                .addMandatoryProperty("field3", "value3")
										                .addOptionalProperty("field4", "value4"))));
		assertThat(json, not(matchesJson(new JsonPattern().addMandatoryProperty("field1", "value1")
										                .addMandatoryProperty("field2", "value2")
										                .addMandatoryProperty("field3", "value3")
										                .addOptionalProperty("field4", startsWith("value")))));
	}

}
