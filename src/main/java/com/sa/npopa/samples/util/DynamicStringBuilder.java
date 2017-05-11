package com.sa.npopa.samples.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;




public class DynamicStringBuilder {

	final String COMMA_SEPARATED_REGEX = "\\s*,\\s*"; //remove spaces around the commas
	final String METHOD_CALL_REGEX = "([a-zA-Z0-9]+)\\(([ ,.a-zA-Z0-9]+)\\)";  //eg. timestampNormalizeToDate(timestamp)
	final String FIXED_STRING_REGEX = "'(.+)'"; //any string in single quotes is fixed text. eg. '|' will be parsed as | 

	private HashMap<String, Method> builderMethods = new HashMap<String, Method>();
	List<DynamicStringBuilderStep> builderSteps = new ArrayList<DynamicStringBuilderStep>();
	JSONParser JSONparser = new JSONParser();

	private String rule;
	private Class UDFClass;
	private Object obj;

	public void setUDFClass(String CustomUDFClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		this.UDFClass=Class.forName(CustomUDFClass);
		this.obj=UDFClass.newInstance();
	}

	public boolean setRule(String rule) throws NoSuchMethodException, SecurityException{

		//String parameter
		Class[] paramString = new Class[1];
		paramString[0] = String.class;
		
		builderMethods.clear();
		builderSteps.clear();

		String[] steps;

		Pattern methodPattern = Pattern.compile(METHOD_CALL_REGEX);
		Matcher methodPatternMatcher ;
		Pattern fixedStringPattern = Pattern.compile(FIXED_STRING_REGEX);
		Matcher fixedStringPatternMatcher ;

		if (rule != null){
			this.rule=rule;
			steps = rule.split(COMMA_SEPARATED_REGEX);

			for (String step : steps){
				methodPatternMatcher = methodPattern.matcher(step);
				fixedStringPatternMatcher = fixedStringPattern.matcher(step);
				DynamicStringBuilderStep d = new DynamicStringBuilderStep();				
				if (methodPatternMatcher.matches()){
					d.setType(DynamicStringBuilderStepType.METHOD_CALL);
					d.setMethod(methodPatternMatcher.group(1));
					d.setAttribute(methodPatternMatcher.group(2));
					builderMethods.put(methodPatternMatcher.group(1), UDFClass.getDeclaredMethod(methodPatternMatcher.group(1), paramString));
				} else if (fixedStringPatternMatcher.matches()){
					d.setType(DynamicStringBuilderStepType.FIXED_STRING);
					d.setAttribute(fixedStringPatternMatcher.group(1));
				} else	{
					d.setType(DynamicStringBuilderStepType.JSON_ATTR);
					d.setAttribute(step);
				}

				builderSteps.add(d);
			}
		}else{
			return false;
		}
		return true;
	}

	public String getRule(){
		return this.rule;
	}
	
	public String getResult(String J) throws ParseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		JSONObject _json = null;

		StringBuilder builder = new StringBuilder();
        _json = (JSONObject) JSONparser.parse(J);
        

		StringBuilder result=new StringBuilder();
		for(DynamicStringBuilderStep step : builderSteps){
			if(step.getType()==DynamicStringBuilderStepType.METHOD_CALL){
				Method method=(Method) builderMethods.get(step.getMethod());
				result.append((String) method.invoke(obj,_json.get(step.getAttribute())));
			}
			if(step.getType()==DynamicStringBuilderStepType.JSON_ATTR){
				result.append(_json.get(step.getAttribute()));
			}
			if(step.getType()==DynamicStringBuilderStepType.FIXED_STRING){
				result.append(step.getAttribute());
			}
		}
		
		return result.toString();
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DynamicStringBuilder: \n");
		for(DynamicStringBuilderStep step : builderSteps){
			sb.append("    "+step.toString()+"\n");
		}
		
		return sb.toString();		
	}



	private static enum DynamicStringBuilderStepType {JSON_ATTR, FIXED_STRING, METHOD_CALL};

	public static class DynamicStringBuilderStep {

		public DynamicStringBuilderStep() {

		}
		private DynamicStringBuilderStepType type;
		private String attribute;
		private String method;

		public DynamicStringBuilderStepType getType() {
			return type;
		}
		public void setType(DynamicStringBuilderStepType type) {
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


}
