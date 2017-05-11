package com.sa.npopa.samples.util;

import com.sa.npopa.samples.ccs.v01.CallStatus;

public class TestDynamicRule {


	public static void main(String[] args) throws Exception {
		final String CLASS="com.sa.npopa.samples.flume.UDFs";
		// final String json="{\"type\":\"ANSWERED\", \"id\":\"000084f8-adce-4bf4-aa1c-b3b39fa2a363\", \"agent\":\"agent12599\", \"callCenter\":\"CC01\", \"timestamp\":\"1486919728663\"}";
		final String json="{\"type\":\"COMPLETED\", \"id\":\"c9f58ca3-6f65-4cf0-a1ba-9b3ac32408da\", \"agent\":\"agent16285\", \"topic\":\"Oozie\", \"subtopic\":\"design\", \"timestamp\":\"1487007029181\"}";


		//final String rule="id,'|',timestampNormalizeToDate(timestamp),'|',timestampDeltaFromDate(timestamp)";
		//final String rule="MD5Prefix2(agent),'|',agent,'|',timestampNormalizeToDate(timestamp)";
		final String[] rules={
				"RandomBytePrefix(),'|',agent,'|',timestamp,'|',timestampNormalizeToDate(timestamp),'|',TS2YYYYMMDDHH(timestamp)",
				"MD5Prefix2(agent),'|',agent,'|',timestamp,'|',timestampNormalizeToDate(timestamp),'|',TS2YYYYMMDDHH(timestamp)"
		};

		//final String rule="type";

		DynamicRule dr = new DynamicRule(); 
		dr.setUDFClass(CLASS);
		for (String rule:rules){
			dr.setRule(rule);
			System.out.println(dr.toString());
			System.out.println(dr.getResult(json));
		}

	}
}
