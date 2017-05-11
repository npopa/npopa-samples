package com.sa.npopa.samples.flume;

import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.auth.FlumeAuthenticationUtil;
import org.apache.flume.auth.PrivilegedExecutor;
import org.apache.flume.sink.hbase.HBaseSinkConfigurationConstants;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedExceptionAction;

import com.google.common.base.Charsets;
import com.sa.npopa.samples.ccs.v01.CallStatus;
import com.sa.npopa.samples.util.DynamicStringBuilder;

import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.parser.ParseException;

public class GenericJSONMetricHBaseSerializer2 extends GenericJSONHBaseSerializer {


	private static final Logger LOGGER = LoggerFactory.getLogger(GenericJSONMetricHBaseSerializer2.class);
	private String tableName;
	private byte[] columnFamily;
	private HTable table;
	private String kerberosPrincipal;
	private String kerberosKeytab;
	private PrivilegedExecutor privilegedExecutor;
	private Configuration config;
	public static final String FLUME_CONFIG_METRIC_TABLE="metric.table";
	public static final String FLUME_CONFIG_METRIC_KEY="metric.key";
	public static final String FLUME_CONFIG_METRIC_COLUMN="metric.column";	
	public static final String FLUME_CONFIG_METRIC_COLUMNFAMILY="metric.columnFamily";
	public static final String FLUME_CONFIG_GET_RETRIES="get.retries";
	public static int getRetries;
	
    final String UDF_CLASS="com.sa.npopa.samples.flume.UDFs";
    
	private byte[] mK; //rowkey
	private byte[] mF; //family - this will be set in the sink
	private byte[] mC; //column
	private byte[] mV; //value - this is the raw JSON

	private DynamicStringBuilder mK_builder = new DynamicStringBuilder();
	private DynamicStringBuilder mC_builder = new DynamicStringBuilder();
    
    DynamicStringBuilder dsb = new DynamicStringBuilder(); 
    
	long startTimeStampL;
	long answerTimeStampL;
	long hungupTimeStampL;
	long onholdTimeStampL;
	long dropTimeStampL;
	long endTimeStampL;
	long lastTransitionTimeStampL;
	
    long ringTime=0;
    long talkTime=0; 
    long holdTime=0;
    long docTime=0;

	
	public GenericJSONMetricHBaseSerializer2() {

	}

	@Override
	public void configure(Context context) {

		super.configure(context);

		//create a table object here.
		kerberosKeytab = context.getString(HBaseSinkConfigurationConstants.CONFIG_KEYTAB);
		kerberosPrincipal = context.getString(HBaseSinkConfigurationConstants.CONFIG_PRINCIPAL);
		tableName = context.getString(FLUME_CONFIG_METRIC_TABLE);
		String cf = context.getString(
				HBaseSinkConfigurationConstants.CONFIG_COLUMN_FAMILY);
		columnFamily = cf.getBytes(Charsets.UTF_8);
        getRetries = context.getInteger(FLUME_CONFIG_GET_RETRIES, 5);
       
        try {
			dsb.setUDFClass(UDF_CLASS);
			mK_builder.setUDFClass(UDF_CLASS);
			mC_builder.setUDFClass(UDF_CLASS);
			
			mK_builder.setRule(context.getString(FLUME_CONFIG_METRIC_KEY));
			mC_builder.setRule(context.getString(FLUME_CONFIG_METRIC_COLUMN));
			
			mF=context.getString(FLUME_CONFIG_METRIC_COLUMNFAMILY).getBytes();
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        
		try {
			privilegedExecutor = FlumeAuthenticationUtil.getAuthenticator(kerberosPrincipal, kerberosKeytab);
		} catch (Exception ex) {
			throw new FlumeException("Failed to login to HBase using "
					+ "provided credentials.", ex);
		}

		config = HBaseConfiguration.create();
		try {
			table = privilegedExecutor.execute(new PrivilegedExceptionAction<HTable>() {


				@Override
				public HTable run() throws Exception {
					HTable table = new HTable(config, tableName);
					table.setAutoFlush(false);
					// Flush is controlled by us. This ensures that HBase changing
					// their criteria for flushing does not change how we flush.
					return table;
				}
			});
		} catch (Exception e) {
			LOGGER.error("Could not load table, " + tableName +
					" from HBase", e);
			throw new FlumeException("Could not load table, " + tableName +
					" from HBase", e);
		}
		try {
			if (!privilegedExecutor.execute(new PrivilegedExceptionAction<Boolean>() {
				@Override
				public Boolean run() throws IOException {
					return table.getTableDescriptor().hasFamily(columnFamily);
				}
			})) {
				throw new IOException("Table " + tableName
						+ " has no such column family " + Bytes.toString(columnFamily));
			}
		} catch (Exception e) {
			//Get getTableDescriptor also throws IOException, so catch the IOException
			//thrown above or by the getTableDescriptor() call.
			throw new FlumeException("Error getting column family from HBase."
					+ "Please verify that the table " + tableName + " and Column Family, "
					+ Bytes.toString(columnFamily) + " exists in HBase, and the"
					+ " current user has permissions to access that table.", e);
		}

	}

	@Override
	public void initialize(Event event, byte[] cf) {

		//check the event exists in HBase
		super.initialize(event, cf);
		mK=null;
		String JSONEvent = new String(event.getBody(), Charsets.UTF_8);
		// do some metric calculation only if "type":"COMPLETED, DROPPED"
		try {
	    dsb.setRule("type");
		String eventT = dsb.getResult(JSONEvent);
		if (eventT.equals(CallStatus.COMPLETED.toString()) || eventT.equals(CallStatus.DROPPED.toString())){
			
		Get get = new Get(this.getK());
		get.addColumn(this.getF(), this.getC());
		Result result;
		boolean found=false;

			for (int i=1; i<=getRetries && !found; i++) //few retries
			{
				result = table.get(get);
				if (result.isEmpty()) {
					LOGGER.debug("Could not find row:[" + new String(this.getK(), StandardCharsets.UTF_8)+"],"+
							" family:[" +new String(this.getF(), StandardCharsets.UTF_8)+"],"+
							" column:[" +new String(this.getC(), StandardCharsets.UTF_8)+"]");
					Thread.sleep(100*i); //back off for 100/200/300/400/500ms ...

				} else {
					
					found=true;
					LOGGER.debug("Found row: [" + new String(this.getK(), StandardCharsets.UTF_8)+"],"+
							" family:[" +new String(this.getF(), StandardCharsets.UTF_8)+"],"+
							" column:[" +new String(this.getC(), StandardCharsets.UTF_8)+"]"); 
					
					// do some metric calculation only if "type":"COMPLETED, DROPPED"
					dsb.setRule("type");
					String dbs_result = dsb.getResult(JSONEvent);
					if (dbs_result.equals(CallStatus.COMPLETED.toString()) || dbs_result.equals(CallStatus.DROPPED.toString())){
						
						mK = mK_builder.getResult(JSONEvent).getBytes();
						mC = mC_builder.getResult(JSONEvent).getBytes();
						//hack together a metrics Json
						Get fullrow_get = new Get(this.getK());
						fullrow_get.addFamily(this.getF());
						Result fullrow_result;
						fullrow_result = table.get(fullrow_get);
						NavigableMap<byte[], byte[]> columnsKV = fullrow_result.getFamilyMap(this.getF());
						
						startTimeStampL=0;
						answerTimeStampL=0;
						onholdTimeStampL=0;
						dropTimeStampL=0;
						hungupTimeStampL=0;
						endTimeStampL=0;
						
						lastTransitionTimeStampL=0;
						CallStatus lastEventType=CallStatus.valueOf("NONE");
						
					    ringTime=0;
					    talkTime=0; 
					    holdTime=0;
					    docTime=0;
					    String id="";
					    String agent="";
					    String callCenter="";
					    String topic="";
					    String subtopic="";


//TODO - I should probably use directly a JSON parser here.
						for (byte[] column : columnsKV.keySet()) {
							dsb.setRule("type");
							String eventType=dsb.getResult(Bytes.toString(columnsKV.get(column)));
							if(eventType.equals(CallStatus.RINGING.toString())){
							     //get start time from it
								dsb.setRule("id");
								id=dsb.getResult(Bytes.toString(columnsKV.get(column)));
								dsb.setRule("timestamp");
								startTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=startTimeStampL;	
							} else if(eventType.equals(CallStatus.DROPPED.toString())) {
								dsb.setRule("timestamp");
								dropTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								if(lastEventType==CallStatus.RINGING) {
									ringTime=dropTimeStampL-lastTransitionTimeStampL;
								} else if (lastEventType==CallStatus.ONHOLD){
									holdTime=holdTime + answerTimeStampL -lastTransitionTimeStampL;
								}
								endTimeStampL=dropTimeStampL;
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=dropTimeStampL;
							}else if(eventType.equals(CallStatus.INPROGRESS.toString())) {
								dsb.setRule("timestamp");
								answerTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								dsb.setRule("agent");
								agent=dsb.getResult(Bytes.toString(columnsKV.get(column)));
								dsb.setRule("callCenter");
								callCenter=dsb.getResult(Bytes.toString(columnsKV.get(column)));
								if(lastEventType==CallStatus.RINGING) {
									ringTime=answerTimeStampL-lastTransitionTimeStampL;
								} else if (lastEventType==CallStatus.ONHOLD){
									holdTime=holdTime + answerTimeStampL -lastTransitionTimeStampL;
								}
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=answerTimeStampL;
							}else if(eventType.equals(CallStatus.ONHOLD.toString())) {
								dsb.setRule("timestamp");
								onholdTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								talkTime=talkTime+onholdTimeStampL-lastTransitionTimeStampL;
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=onholdTimeStampL;
							}else if(eventType.equals(CallStatus.FINISHED.toString())) {
								dsb.setRule("timestamp");
								hungupTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								talkTime=talkTime+hungupTimeStampL-lastTransitionTimeStampL;
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=hungupTimeStampL;
							}else if(eventType.equals(CallStatus.COMPLETED.toString())) {
								dsb.setRule("timestamp");
								endTimeStampL=Long.parseLong(dsb.getResult(Bytes.toString(columnsKV.get(column))));
								dsb.setRule("topic");
								topic=dsb.getResult(Bytes.toString(columnsKV.get(column)));
								dsb.setRule("subtopic");
								subtopic=dsb.getResult(Bytes.toString(columnsKV.get(column)));
								docTime=docTime+endTimeStampL-lastTransitionTimeStampL;
								lastEventType=CallStatus.valueOf(eventType);
								lastTransitionTimeStampL=endTimeStampL;
							}

							//column -> Bytes.toString(column)
							//value -> Bytes.toString(columnsKV.get(column))
						}
						StringBuilder sb = new StringBuilder();
						sb.append("{");						
						sb.append("\"id\":\"" + id + "\", ");
						sb.append("\"start\":\"" + startTimeStampL + "\", "); //I should probably convert these to dates
						sb.append("\"end\":\"" + endTimeStampL + "\", ");
						sb.append("\"ringTime\":\"" + ringTime + "\", ");
						sb.append("\"talkTime\":\"" + talkTime + "\", ");
						sb.append("\"holdTime\":\"" + holdTime + "\", ");
						sb.append("\"docTime\":\"" + docTime + "\", ");
						sb.append("\"totalTime\":\"" + (endTimeStampL-startTimeStampL) + "\", ");
						sb.append("\"totalCalculatedTime\":\"" + (ringTime+talkTime+holdTime+docTime) + "\", ");
						sb.append("\"agent\":\"" + agent + "\", ");	
						sb.append("\"callCenter\":\"" + callCenter + "\", ");	
						sb.append("\"topic\":\"" + topic + "\", ");	
						sb.append("\"subtopic\":\"" + subtopic + "\", ");
						sb.append("\"metricType\":\"CALL\"" ); //hack a last line so I don't have to keep track of commas
						sb.append("}");
						LOGGER.debug("Calculated metric event: "+sb.toString());
						mV=sb.toString().getBytes();
						
						/**
						StringBuilder sb = new StringBuilder();
						sb.append(Bytes.toString(mK) + ",");
						NavigableMap<byte[], NavigableMap<byte[], byte[]>> fMap = fullrow_result.getNoVersionMap();
						for (byte[] familyBytes : fMap.keySet()) {
							NavigableMap<byte[], byte[]> qMap = fMap.get(familyBytes);
							for (byte[] qualifier : qMap.keySet()) {
								sb.append("[" + Bytes.toString(familyBytes) + ":" + Bytes.toString(qualifier) + "="
										+ Bytes.toString(qMap.get(qualifier)) + "] ");
							}
						}						
						LOGGER.debug(sb.toString());
						*/
						
					}

				}

			}
			if (!found){
				LOGGER.error("Giving up on: [" + new String(this.getK(), StandardCharsets.UTF_8)+"],"+
							" family:[" +new String(this.getF(), StandardCharsets.UTF_8)+"],"+
							" column:[" +new String(this.getC(), StandardCharsets.UTF_8)+"] after "+getRetries+" retries."); 
 
			}
		}
		} catch (IOException | InterruptedException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}


	@Override
	public List<Row> getActions() throws FlumeException {
		List<Row> actions = new LinkedList<Row>();
		if(mK != null) {
			try {
				Put put = new Put(mK);
				put.add(mF, mC, mV);
				actions.add(put);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return actions;
	}



	@Override
	public List<Increment> getIncrements() {
		List<Increment> incs = new LinkedList<Increment>();
        //no increments 
		return incs;
	}


	@Override
	public void close() {
		try {
			if (table != null) {
				table.close();
			}
			table = null;
		} catch (IOException e) {
			throw new FlumeException("Error closing table.", e);
		}

	}

}
