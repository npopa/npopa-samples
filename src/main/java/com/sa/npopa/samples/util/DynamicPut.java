package com.sa.npopa.samples.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.hadoop.hbase.client.Put;
import org.json.simple.parser.ParseException;

public class DynamicPut {
	
	//not used
	byte[] table;	

	

	String CustomUDFClass;	

	private HashMap<String, DynamicRule> rules = new HashMap<String, DynamicRule>();
	
	public void addRule(String type,String rule){
		
		DynamicRule dr = new DynamicRule();
		try {
			dr.setUDFClass(CustomUDFClass);
			dr.setRule(rule);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		rules.put(type, dr);
	}	

	Put get(String event){
		
		Put p = null;
		byte[] family = null;
		byte[] key = null;	
		byte[] column = null;
		byte[] value = null;
		
		try {

			if (rules.get("Family") != null){
				family=rules.get("Family").getResult(event).getBytes();
			} else {
				family = "F".getBytes();
			}
			
			if (rules.get("Key") != null && rules.get("Key").getResult(event) != null){
				key=rules.get("Key").getResult(event).getBytes();
			}
			
			if (rules.get("Column") != null && rules.get("Column").getResult(event) != null){
			    column=rules.get("Column").getResult(event).getBytes();
			}
			
			if (rules.get("Value") != null && rules.get("Value").getResult(event) != null){
				value=rules.get("Value").getResult(event).getBytes();
			} else {
				value = event.getBytes();
			}
			
			if (key!=null && family!=null && column!=null){
				p = new Put(key);
				p.addColumn(family, column, value);
			}
			
			

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return p;
	}
	
	
	public String getCustomUDFClass() {
		return CustomUDFClass;
	}

	public void setCustomUDFClass(String customUDFClass) {
		CustomUDFClass = customUDFClass;
	}


	@Override
	public String toString() {
		return "DynamicPut [rules=" + rules.toString() + "]";
	}
	

	
}
