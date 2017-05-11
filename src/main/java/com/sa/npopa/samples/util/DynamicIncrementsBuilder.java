package com.sa.npopa.samples.util;


import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicIncrementsBuilder {
	
	String CustomUDFClass;
	ImmutableMap<String, String> incrementsProperties;	
	private HashMap<String, DynamicIncrement> increments = new HashMap<String, DynamicIncrement>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicIncrementsBuilder.class);
	
	public DynamicIncrementsBuilder(){
		
	}
	
	public void setIncrementsProperties(ImmutableMap<String, String> incrementsProperties){
		this.incrementsProperties=incrementsProperties;
	}
	
	public void build(){
		for (String prop: incrementsProperties.keySet()){
		
			String incrementName = prop.split("\\.")[0];
			String incrementRuleName = prop.split("\\.")[1];
			
			if (increments.containsKey(incrementName)) {
				increments.get(incrementName).addRule(incrementRuleName, incrementsProperties.get(prop));
			} else {
				DynamicIncrement di = new DynamicIncrement();
				di.setCustomUDFClass(CustomUDFClass);
				di.addRule(incrementRuleName, incrementsProperties.get(prop));
				increments.put(incrementName, di);
			}
		}
	}
    
	public List<Increment> get(String event){
		
		LOGGER.trace("Building puts for:" + event);
		
		List<Increment> incs = new LinkedList<Increment>();
		for (String incrementName: increments.keySet()){
			if(increments.get(incrementName) != null){
				Increment i = increments.get(incrementName).get(event);
				if (i!=null){
				incs.add(i);
				LOGGER.trace("INC:"+i.toString());
				}
				
			}
		}
		 
		
		return incs;
		
	}
	
	public String getCustomUDFClass() {
		return CustomUDFClass;
	}

	public void setCustomUDFClass(String customUDFClass) {
		CustomUDFClass = customUDFClass;
	}
	
}
