package com.sa.npopa.samples.flume;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.HbaseEventSerializer;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.sa.npopa.samples.util.DynamicIncrementsBuilder;
import com.sa.npopa.samples.util.DynamicPutsBuilder;
import com.sa.npopa.samples.util.DynamicStringBuilder;

public class GenericJSONHBaseSerializer2 implements HbaseEventSerializer {


	private static final Logger LOGGER = LoggerFactory.getLogger(GenericJSONHBaseSerializer2.class);

	final String UDF_CLASS="com.sa.npopa.samples.flume.UDFs";

	
	private boolean hbasePutsDryRun=false;
	private boolean hbaseIncrementsDryRun=false;
	
	private DynamicPutsBuilder puts_builder = new DynamicPutsBuilder();
	private DynamicIncrementsBuilder increments_builder = new DynamicIncrementsBuilder();
	
	public static final String FLUME_CONFIG_PUTS="puts.";
	public static final String FLUME_CONFIG_INCREMENTS="increments.";
	public static final String FLUME_CONFIG_HBASE_PUTS_DRYRUN="hbasePutsDryRun";
	public static final String FLUME_CONFIG_HBASEINCREMENTS_DRYRUN="hbaseIncrementsDryRun";
	
	byte[] defaultFamily = null;
	String theEvent;
	
	public GenericJSONHBaseSerializer2() {

	}

	@Override
	public void configure(Context context) {

		try {
			
			puts_builder.setCustomUDFClass(UDF_CLASS);			
			ImmutableMap <String,String> putsProps=context.getSubProperties(FLUME_CONFIG_PUTS);
			LOGGER.info("Puts props:"+putsProps.toString());
			puts_builder.setPutsProperties(putsProps);
			puts_builder.build();
			
			increments_builder.setCustomUDFClass(UDF_CLASS);			
			ImmutableMap <String,String> incrementsProps=context.getSubProperties(FLUME_CONFIG_INCREMENTS);
			LOGGER.info("Increments props:"+incrementsProps.toString());
			increments_builder.setIncrementsProperties(incrementsProps);
			increments_builder.build();			
			
			hbasePutsDryRun=context.getBoolean(FLUME_CONFIG_HBASE_PUTS_DRYRUN, false);			
			hbaseIncrementsDryRun=context.getBoolean(FLUME_CONFIG_HBASEINCREMENTS_DRYRUN, false);			
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	@Override
	public void initialize(Event event, byte[] cf) {


	    this.theEvent = new String(event.getBody(), Charsets.UTF_8);
		
		this.defaultFamily = cf;

	}

	@Override
	public List<Row> getActions() throws FlumeException {
	    //in case of a dry run evaluate all the rules and compute the puts but return an empty list.
		
		List<Row> actions = puts_builder.get(theEvent);		
		if(!hbasePutsDryRun){
			return actions;
		}
		
		return new LinkedList<Row>();
		
	}



	@Override
	public List<Increment> getIncrements() {
		//TODO - seems to be some performance issue with increments - Maybe this needs to be disabled by default.
		//in case of a dry run evaluate all the rules and compute the puts but return an empty list.

		List<Increment> incs = increments_builder.get(theEvent);
		if (!hbaseIncrementsDryRun){
			return incs;
		}
		return new LinkedList<Increment>();
	}

	@Override
	public void configure(ComponentConfiguration arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
