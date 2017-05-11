package com.sa.npopa.samples.util;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Row;

import com.google.common.collect.ImmutableMap;

public class TestDynamicIncrementsBuilder {


	public static void main(String[] args) throws Exception {

		
		
		final String CLASS="com.sa.npopa.samples.flume.UDFs";
		final String[] agent_events= {
				"{\"agent\":\"agent08905\", \"timestamp\":\"1487604205850\", \"status\":\"AVAILABLE\"}",
				"{\"agent\":\"agent50223\", \"timestamp\":\"1487604205887\", \"status\":\"AVAILABLE\"}",
				"{\"agent\":\"agent37184\", \"timestamp\":\"1487604205942\", \"status\":\"TALKING\"}",
				"{\"agent\":\"agent61037\", \"timestamp\":\"1487604205988\", \"status\":\"TALKING\"}",
				"{\"agent\":\"agent60596\", \"timestamp\":\"1487604206007\", \"status\":\"TALKING\"}",
				"{\"agent\":\"agent59375\", \"timestamp\":\"1487604206019\", \"status\":\"DOCUMENTING\"}",
				"{\"agent\":\"agent16014\", \"timestamp\":\"1487604206053\", \"status\":\"TALKING\"}",
				"{\"agent\":\"agent87111\", \"timestamp\":\"1487604206067\", \"status\":\"AVAILABLE\"}",
				"{\"agent\":\"agent13695\", \"timestamp\":\"1487604206079\", \"status\":\"DOCUMENTING\"}",
				"{\"agent\":\"agent98217\", \"timestamp\":\"1487604206081\", \"status\":\"DOCUMENTING\"}"
		};
          

//This is for Agent		
		ImmutableMap<String,String> ip_agent = ImmutableMap.<String, String>builder()
				.put("01.Family", "'J'")
				.put("01.Key", "MD5Prefix2(agent),'|',agent,'|',TS2YYYYMMDDHH(timestamp)")
				.put("01.Column", "timestamp") //this gets the default increment value 1
				.put("02.Family", "'J'")
				.put("02.Key", "MD5Prefix2(agent),'|',TS2YYYYMMDDHH(timestamp)")
				.put("02.Column", "agent")
				.put("02.Value", "GetAgentIncrement(status)")
			    .build();
			
				
		DynamicIncrementsBuilder dib_agent = new DynamicIncrementsBuilder();
		dib_agent.setCustomUDFClass(CLASS);
		dib_agent.setIncrementsProperties(ip_agent);		
		dib_agent.build();
		
		for(String agent_event : agent_events){
			System.out.println("\nAgent Event:" + agent_event);
		List<Increment> results = dib_agent.get(agent_event);
		for (Increment result : results){
			System.out.println("    Increment:"+result.toString());
		}
		}

		final String[] call_events= {	
		"{\"type\":\"INPROGRESS\", \"id\":\"370ea521-32c0-4dee-9971-bae42602afc4\", \"agent\":\"agent67276\", \"callCenter\":\"Chicago\", \"timestamp\":\"1487604202832\"}",
		"{\"type\":\"ONHOLD\", \"id\":\"3ec99b67-989d-49b7-8ab6-fcfc9d2611b6\", \"agent\":\"agent15503\", \"timestamp\":\"1487604202834\"}",
		"{\"type\":\"FINISHED\", \"id\":\"dd943982-dd76-4823-bdc4-074514c07194\", \"agent\":\"agent25201\", \"timestamp\":\"1487604202856\"}",
		"{\"type\":\"ONHOLD\", \"id\":\"cd781146-c56e-4f6e-a35a-b701b47a4c1e\", \"agent\":\"agent24977\", \"timestamp\":\"1487604202860\"}",
		"{\"type\":\"FINISHED\", \"id\":\"88c5303d-000f-4ad6-a445-ac39d7a8e033\", \"agent\":\"agent60756\", \"timestamp\":\"1487604202901\"}",
		"{\"type\":\"INPROGRESS\", \"id\":\"d5584dc4-75d6-41b9-82f8-75e8b60486f9\", \"agent\":\"agent40609\", \"callCenter\":\"Austin\", \"timestamp\":\"1487604202920\"}",
		"{\"type\":\"ONHOLD\", \"id\":\"ec7deb9c-1a5e-462d-825e-289d3012b8f8\", \"agent\":\"agent03790\", \"timestamp\":\"1487604202962\"}",
		"{\"type\":\"INPROGRESS\", \"id\":\"43b33b50-40af-4174-b15b-7a30e154c512\", \"agent\":\"agent76877\", \"callCenter\":\"Palo Alto\", \"timestamp\":\"1487604202985\"}",
		"{\"type\":\"RINGING\", \"id\":\"18d672be-121f-410e-babf-7e1011bcf3ef\", \"timestamp\":\"1487604203000\"}",
		"{\"type\":\"COMPLETED\", \"id\":\"ee3e15e7-4aad-4f88-9504-7c9b97f89cb0\", \"agent\":\"agent82149\", \"topic\":\"HIVE\", \"subtopic\":\"documentation\", \"timestamp\":\"1487604203024\"}"
	};
		

//this is for Call
		ImmutableMap<String,String> ip_call = ImmutableMap.<String, String>builder()
				.put("03.Family", "'J'")
				.put("03.Key", "MD5Prefix2(agent),'|',agent,'|',TS2YYYYMMDDHH(timestamp)")
				.put("03.Column", "id")
				.put("03.Value", "GetCallIncrement(type)")
			    .build();	
		
		DynamicIncrementsBuilder dib_call = new DynamicIncrementsBuilder();
		dib_call.setCustomUDFClass(CLASS);
		dib_call.setIncrementsProperties(ip_call);		
		dib_call.build();
		
		for(String call_event : call_events){
			System.out.println("\nCall Event:" + call_event);
		List<Increment> results = dib_call.get(call_event);
		for (Increment result : results){
			System.out.println("    Increment:"+result.toString());
		}
		}


	}
}
