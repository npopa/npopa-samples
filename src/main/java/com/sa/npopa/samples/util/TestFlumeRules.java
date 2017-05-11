package com.sa.npopa.samples.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Charsets;


public class TestFlumeRules {

	private static enum attrType {JSON_ATTR, FIXED_STRING, METHOD_CALL};
	
	public static class DynamicStringBuilderStep {
		
		public DynamicStringBuilderStep() {

		}
		private attrType type;
		private String attribute;
		private String method;
		
		public attrType getType() {
			return type;
		}
		public void setType(attrType type) {
			this.type = type;
		}
		public String getAttribute() {
			return attribute;
		}
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		@Override
		public String toString() {
			return "DynamicStringBuilderStep [type=" + type + ", attribute=" + attribute + ", method=" + method + "]";
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {

        final String rule="id,'|',timestampNormalizeToDate(timestamp),'|',timestampDeltaFromDate(timestamp)";
        final String json="{\"type\":\"ANSWERED\", \"id\":\"000084f8-adce-4bf4-aa1c-b3b39fa2a363\", \"agent\":\"agent12599\", \"callCenter\":\"CC01\", \"timestamp\":\"1486554367663\"}";
        
    	final String COMMA_SEPARATED_REGEX = "\\s*,\\s*"; //remove spaces around the commas
        final String METHOD_CALL_REGEX = "([a-zA-Z0-9]+)\\(([ ,.a-zA-Z0-9]+)\\)";  //eg. timestampNormalizeToDate(timestamp)
    	final String FIXED_STRING_REGEX = "'(.+)'"; //any string in single quotes is fixed text. eg. '|' will be parsed as | 
    	

	    HashMap methods = new HashMap();
		//String parameter
		Class[] paramString = new Class[1];
		paramString[0] = String.class;
		
		Class cls = Class.forName("com.sa.npopa.samples.flume.UDFs");
		Object obj = cls.newInstance();
    	
		String rowkey_template;
		String[] K_attributesConfig;
		List<DynamicStringBuilderStep> K_BuilderAttributes = new ArrayList<DynamicStringBuilderStep>();
        Pattern methodPattern = Pattern.compile(METHOD_CALL_REGEX);
        Matcher methodPatternMatcher ;
        Pattern fixedStringPattern = Pattern.compile(FIXED_STRING_REGEX);
        Matcher fixedStringPatternMatcher ;
		
		if (rule != null){
			K_attributesConfig = rule.split(COMMA_SEPARATED_REGEX);

			for (String K_attribute : K_attributesConfig){
				methodPatternMatcher = methodPattern.matcher(K_attribute);
				fixedStringPatternMatcher = fixedStringPattern.matcher(K_attribute);
				if (methodPatternMatcher.matches()){
					DynamicStringBuilderStep d = new DynamicStringBuilderStep();
					d.setType(attrType.METHOD_CALL);
					d.setMethod(methodPatternMatcher.group(1));
					d.setAttribute(methodPatternMatcher.group(2));
					K_BuilderAttributes.add(d);
					methods.put(methodPatternMatcher.group(1), cls.getDeclaredMethod(methodPatternMatcher.group(1), paramString));
				} else if (fixedStringPatternMatcher.matches()){
					DynamicStringBuilderStep d = new DynamicStringBuilderStep();
					d.setType(attrType.FIXED_STRING);
					d.setAttribute(fixedStringPatternMatcher.group(1));
					K_BuilderAttributes.add(d);
				} else	{
					DynamicStringBuilderStep d = new DynamicStringBuilderStep();
					d.setType(attrType.JSON_ATTR);
					d.setAttribute(K_attribute);
					K_BuilderAttributes.add(d);
				}
			}
		}
	    //System.out.println(Arrays.asList(K_BuilderAttributes).toString());
	    
	    //now let's try to build keys with our rule
		JSONObject _json = null;
		JSONParser parser = new JSONParser();
		StringBuilder builder = new StringBuilder();
        _json = (JSONObject) parser.parse(json);
        

		StringBuilder result=new StringBuilder();
		for(DynamicStringBuilderStep a: K_BuilderAttributes){
			if(a.getType()==attrType.METHOD_CALL){
				Method method=(Method) methods.get(a.getMethod());
				result.append((String) method.invoke(obj,_json.get(a.getAttribute())));
			}
			if(a.getType()==attrType.JSON_ATTR){
				result.append(_json.get(a.getAttribute()));
			}
			if(a.getType()==attrType.FIXED_STRING){
				result.append(a.getAttribute());
			}
		}
		System.out.println(result.toString());
        
        
        
	}
}
