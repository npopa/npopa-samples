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


public class DynamicRule {

	final String COMMA_SEPARATED_REGEX = "\\s*,\\s*"; //remove spaces around the commas
	final String METHOD_CALL_REGEX = "([a-zA-Z0-9]+)\\(([ ,.a-zA-Z0-9]*)\\)";  //eg. timestampNormalizeToDate(timestamp)
	final String FIXED_STRING_REGEX = "'(.+)'"; //any string in single quotes is fixed text. eg. '|' will be parsed as | 
	
	private String rule;

	private HashMap<String, Method> ruleMethods = new HashMap<String, Method>();
	private List<RuleStep> ruleSteps = new ArrayList<RuleStep>();

	private Class UDFClass;
	private Object obj;
	JSONParser JSONparser = new JSONParser();

	public void setUDFClass(String CustomUDFClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		this.UDFClass=Class.forName(CustomUDFClass);
		this.obj=UDFClass.newInstance();
	}
	
	public boolean setRule(String rule) throws NoSuchMethodException, SecurityException{

		//String parameter
		Class[] paramString = new Class[1];
		paramString[0] = String.class;
		
		ruleMethods.clear();
		ruleSteps.clear();

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
				RuleStep s = new RuleStep();				
				if (methodPatternMatcher.matches()){
					s.setType(StepType.METHOD_CALL);
					s.setMethod(methodPatternMatcher.group(1));
					s.setAttribute(methodPatternMatcher.group(2));
					ruleMethods.put(methodPatternMatcher.group(1), UDFClass.getDeclaredMethod(methodPatternMatcher.group(1), paramString));
				} else if (fixedStringPatternMatcher.matches()){
					s.setType(StepType.FIXED_STRING);
					s.setAttribute(fixedStringPatternMatcher.group(1));
				} else	{
					s.setType(StepType.JSON_ATTR);
					s.setAttribute(step);
				}

				ruleSteps.add(s);
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

        _json = (JSONObject) JSONparser.parse(J);
        

		StringBuilder result=new StringBuilder();
		for(RuleStep step : ruleSteps){
			if(step.getType()==StepType.METHOD_CALL){
				//TODO need to add a check for missing attributes and return null. Upstream should ignore the put request.
				Method method=(Method) ruleMethods.get(step.getMethod());
				String arg = (String) _json.get(step.getAttribute());
				if (arg != null){
				result.append( method.invoke(obj,arg));
				} else {
					return null;
				}
			}
			if(step.getType()==StepType.JSON_ATTR){
				String arg = (String) _json.get(step.getAttribute());
				if (arg != null){
				result.append(_json.get(step.getAttribute()));
				}else {
					return null;
				}
			}
			if(step.getType()==StepType.FIXED_STRING){
				result.append(step.getAttribute());
			}
		}
		
		return result.toString();
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DynamicRule: \n");
		for(RuleStep step : ruleSteps){
			sb.append("    "+step.toString()+"\n");
		}
		
		return sb.toString();		
	}
	

	



public static enum StepType {JSON_ATTR, FIXED_STRING, METHOD_CALL};

public static class RuleStep {

	public RuleStep() {

	}
	private StepType type;
	private String attribute;
	private String method;

	public StepType getType() {
		return type;
	}
	public void setType(StepType type) {
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
		return "RuleStep [type=" + type + ", attribute=" + attribute + ", method=" + method + "]";
	}

}

}