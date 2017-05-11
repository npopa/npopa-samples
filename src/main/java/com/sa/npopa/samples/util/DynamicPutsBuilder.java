package com.sa.npopa.samples.util;


import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicPutsBuilder {
	
	String CustomUDFClass;
	ImmutableMap<String, String> putsProperties;	
	private HashMap<String, DynamicPut> puts = new HashMap<String, DynamicPut>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPutsBuilder.class);
	
	public DynamicPutsBuilder(){
		
	}
	
	public void setPutsProperties(ImmutableMap<String, String> putsProperties){
		this.putsProperties=putsProperties;
	}
	
	public void build(){
		for (String prop: putsProperties.keySet()){
		
			String putName = prop.split("\\.")[0];
			String putRuleName = prop.split("\\.")[1];
			
			if (puts.containsKey(putName)) {
				puts.get(putName).addRule(putRuleName, putsProperties.get(prop));
			} else {
				DynamicPut dp = new DynamicPut();
				dp.setCustomUDFClass(CustomUDFClass);
				dp.addRule(putRuleName, putsProperties.get(prop));
				puts.put(putName, dp);
			}
		}
	}
    
	public List<Row> get(String event){
		
		LOGGER.trace("Building puts for:" + event);
		
		List<Row> actions = new LinkedList<Row>();
		for (String putName: puts.keySet()){
			if(puts.get(putName) != null){
				Put p = puts.get(putName).get(event);
				if (p!=null){
				actions.add(p);
				LOGGER.trace("PUT:"+p.toString());
				}
				
			}
		}
		 
		
		return actions;
		
	}
	
	public String getCustomUDFClass() {
		return CustomUDFClass;
	}

	public void setCustomUDFClass(String customUDFClass) {
		CustomUDFClass = customUDFClass;
	}
	
}
