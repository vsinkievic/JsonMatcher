package com.bpmid.matchers;

import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMatcher extends TypeSafeMatcher<String> {
	
	public static ObjectMapper objectMapper;
	
	private JsonPattern expectedJson;
	private String diffDescription = null;
	
	JsonMatcher(JsonPattern expectedJson){
		this.expectedJson = expectedJson;
	}

	//@Override
	public void describeTo(Description description) {
	//	description.appendText("Matches my JSON:");
		description.appendText(expectedJson.toDescription());
		if (diffDescription != null)
			description.appendText("Diff description: " + diffDescription + "\n");
	}

	@Override
	protected boolean matchesSafely(String actualJsonString) {
		if (objectMapper == null)
			objectMapper = new ObjectMapper();
		JsonNode actualJson;
		try {
			actualJson = objectMapper.readTree(actualJsonString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		if (expectedJson.isSimilarTo(actualJson)) 
			return true;
		else {
			this.diffDescription = expectedJson.getDiffDescription();
			return false;
		}
	}
	
	public static Matcher<String> matchesJson(JsonPattern expectedJson){
		return new JsonMatcher(expectedJson);
	}

}
