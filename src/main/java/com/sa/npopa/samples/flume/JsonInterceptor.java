package com.sa.npopa.samples.flume;

/**
This expects a JSON message and extracts attributes from it to the header.
Attribute names are passed using extractAttributes property. If the attribute is not present
it will not be extracted.

eg.
agent1.sources.kafka-r1.interceptors = agent1-i1
agent1.sources.kafka-r1.interceptors.agent1-i1.type = com.cloudera.sa.ylp.flume.JsonInterceptor$Builder
agent1.sources.kafka-r1.interceptors.agent1-i1.extractAttributes = date, type, business_id, user_id, review_id

*/

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Charsets;

public class JsonInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JsonInterceptor.class);
	private JSONParser parser;
	public static final String REGEX = "\\s*,\\s*";
	private String[] attributes;



	/**
	 * Only {@link JsonInterceptor.Builder} can build me
	 */
	private JsonInterceptor(String extractAttributes) {
		this.parser = new JSONParser();
		this.attributes = extractAttributes.split(REGEX);
	}

	@Override
	public void initialize() {
		// no-op
	}

	/**
	 * Modifies events in-place.
	 */
	@Override
	public Event intercept(Event event) {

		if (this.attributes != null) //have something to extract
			{
			
			Map<String, String> headers = event.getHeaders();
			String body = new String(event.getBody(), Charsets.UTF_8);		
			String attribute_value=null;
			JSONObject _json = null;
	
			try {
				_json = (JSONObject) parser.parse(body);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (String attribute_key : attributes){
				attribute_value = (String) _json.get(attribute_key);
				if (attribute_value != null)
		           headers.put(attribute_key,attribute_value);
	
			}

		}
		return event;
	}

	/**
	 * Delegates to {@link #intercept(Event)} in a loop.
	 * 
	 * @param events
	 * @return
	 */
	@Override
	public List<Event> intercept(List<Event> events) {
		for (Event event : events) {
			intercept(event);
		}
		return events;
	}

	@Override
	public void close() {
		// no-op
	}

	/**
	 * Builder which builds new instances of the InterceptorTemplate.
	 */
	public static class Builder implements Interceptor.Builder {

		private String extractAttributes = null;

		@Override
		public Interceptor build() {
			return new JsonInterceptor(extractAttributes);
		}

		@Override
		public void configure(Context context) {
			extractAttributes = context.getString(Constants.EXTRACT_ATTRIBUTES, null);
		}

	}

	public static class Constants {
		public static String EXTRACT_ATTRIBUTES = "extractAttributes";
		
	}

}
