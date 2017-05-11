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
import com.sa.npopa.samples.util.DynamicStringBuilder;

public class GenericJSONHBaseSerializer implements HbaseEventSerializer {


	private static final Logger LOGGER = LoggerFactory.getLogger(GenericJSONHBaseSerializer.class);

	final String UDF_CLASS="com.sa.npopa.samples.flume.UDFs";
	//payload
	private byte[] K; //rowkey
	private byte[] F; //family - this will be set in the sink
	private byte[] C; //column
	private byte[] V; //value - this is the raw JSON
	//increments
	private byte[] iK; //increments rowkey
	private byte[] iC; //increments column
	private long iV=1; //increments value
	
	private boolean hbaseDryRun=false;
	private boolean hbaseIncrementsDryRun=false;
	
	private DynamicStringBuilder K_builder = new DynamicStringBuilder();
	private DynamicStringBuilder C_builder = new DynamicStringBuilder();
	private DynamicStringBuilder iK_builder = new DynamicStringBuilder();
	private DynamicStringBuilder iC_builder = new DynamicStringBuilder();
	private DynamicStringBuilder iV_builder = new DynamicStringBuilder();
	
	public static final String FLUME_CONFIG_KEY="payloadKey";
	public static final String FLUME_CONFIG_COLUMN="payloadColumn";
	public static final String FLUME_CONFIG_IKEY="incrementKey";
	public static final String FLUME_CONFIG_ICOLUMN="incrementColumn";
	public static final String FLUME_CONFIG_IVALUE="incrementValue";
	public static final String FLUME_CONFIG_HBASE_DRYRUN="hbaseDryRun";
	public static final String FLUME_CONFIG_HBASEINCREMENT_SDRYRUN="hbaseIncrementsDryRun";
	
	public GenericJSONHBaseSerializer() {

	}

	@Override
	public void configure(Context context) {

		try {
			//TODO - I can assume all use the same UDF class and set it default
			K_builder.setUDFClass(UDF_CLASS);
			C_builder.setUDFClass(UDF_CLASS);
			iK_builder.setUDFClass(UDF_CLASS);
			iC_builder.setUDFClass(UDF_CLASS);
			iV_builder.setUDFClass(UDF_CLASS);
			
			//TODO - should I build an hash with all builders?
			K_builder.setRule(context.getString(FLUME_CONFIG_KEY));
			C_builder.setRule(context.getString(FLUME_CONFIG_COLUMN));
			iK_builder.setRule(context.getString(FLUME_CONFIG_IKEY));
			iC_builder.setRule(context.getString(FLUME_CONFIG_ICOLUMN));
			iV_builder.setRule(context.getString(FLUME_CONFIG_IVALUE));
			
			
			
			hbaseDryRun=context.getBoolean(FLUME_CONFIG_HBASE_DRYRUN, false);			
			hbaseIncrementsDryRun=context.getBoolean(FLUME_CONFIG_HBASEINCREMENT_SDRYRUN, false);			
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	@Override
	public void initialize(Event event, byte[] cf) {


		String JSONEvent = new String(event.getBody(), Charsets.UTF_8);
		try {
			K = K_builder.getResult(JSONEvent).getBytes();
			C = C_builder.getResult(JSONEvent).getBytes();
			V = event.getBody();

			iK = iK_builder.getResult(JSONEvent).getBytes();
			iC = iC_builder.getResult(JSONEvent).getBytes();
			iV = Long.parseLong(iV_builder.getResult(JSONEvent));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.F = cf;

	}

	@Override
	public List<Row> getActions() throws FlumeException {
		List<Row> actions = new LinkedList<Row>();
		if(!hbaseDryRun){
			try {
				Put put = new Put(K);
				put.add(F, C, V);
				actions.add(put);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return actions;
	}



	@Override
	public List<Increment> getIncrements() {
		//TODO - seems to be some performance issue with increments - Maybe this needs to be disabled by default.
		List<Increment> incs = new LinkedList<Increment>();
		if (!hbaseIncrementsDryRun){
			if(iC != null) {
				Increment inc = new Increment(iK);
				inc.addColumn(F, iC, iV);
				incs.add(inc);
			}
		}
		return incs;
	}

	@Override
	public void configure(ComponentConfiguration arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public byte[] getK() {
		return K;
	}

	public void setK(byte[] k) {
		K = k;
	}

	public byte[] getF() {
		return F;
	}

	public void setF(byte[] f) {
		F = f;
	}

	public byte[] getC() {
		return C;
	}

	public void setC(byte[] c) {
		C = c;
	}

	public byte[] getV() {
		return V;
	}

	public void setV(byte[] v) {
		V = v;
	}

	public byte[] getiK() {
		return iK;
	}

	public void setiK(byte[] iK) {
		this.iK = iK;
	}

	public byte[] getiC() {
		return iC;
	}

	public void setiC(byte[] iC) {
		this.iC = iC;
	}

}
