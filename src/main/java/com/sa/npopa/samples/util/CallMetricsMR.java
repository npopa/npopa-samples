package com.sa.npopa.samples.util;

import java.io.IOException;
import java.util.NavigableMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.IdentityTableReducer;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.*;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import com.sa.npopa.samples.ccs.v01.CallStatus;

import org.apache.hadoop.hbase.client.Scan;

/*

export CLASSPATH=`hbase classpath`:$CLASSPATH
export CLASSPATH=`hadoop classpath`:$CLASSPATH
export HADOOP_CLASSPATH=$CLASSPATH:$HADOOP_CLASSPATH

export OUTPUT_PATH=/tmp/20170218

hdfs dfs -rm -R -skipTrash $OUTPUT_PATH 

yarn jar /var/lib/flume-ng/plugins.d/agent1/lib/npopa-samples-0.0.1-SNAPSHOT.jar \
   com.sa.npopa.samples.util.CallMetricsMR \
   -libjars /opt/cloudera/parcels/CDH/jars/json-simple-1.1.1.jar \
   -Dmapreduce.job.maps=8 \
   --outputPath $OUTPUT_PATH 

hdfs dfs -ls $OUTPUT_PATH

 */

public class CallMetricsMR extends Configured implements Tool {

	private Options options = new Options();
	private long numRecords=0;
	private String outputPath;
	private String inputPath;
	private static String tableName = "call";
	public enum Counters { CALLS, METERED, PERIODS, RE_METERED,NOT_RE_METERED }
	private static final Logger LOGGER = LoggerFactory.getLogger(CallMetricsMR.class);

	public static class CallMetricsMapper 
	extends Mapper<LongWritable, NullWritable, ImmutableBytesWritable, Mutation> {

		private ImmutableBytesWritable call_key = new ImmutableBytesWritable();
		private ImmutableBytesWritable callstatus_key = new ImmutableBytesWritable();
		private NullWritable ignored = NullWritable.get();
		private Connection connection = null;
		private Table t1Table = null;
		private JSONParser parser = new JSONParser();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			connection = ConnectionFactory.createConnection(context.getConfiguration());
			t1Table = connection.getTable(TableName.valueOf(tableName));
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			t1Table.close();
		}


		public void map(LongWritable partition, NullWritable ignored,
				Context context) throws IOException, InterruptedException {
			
			
			Filter filter1 = new ValueFilter(CompareOp.EQUAL, 
					new SubstringComparator("COMPLETED"));	
			Filter filter2 = new ValueFilter(CompareOp.EQUAL, 
					new SubstringComparator("DROPPED"));
			FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE,filter1,filter2);
			
			Scan callScan = new Scan()
					.setRowPrefixFilter(Bytes.toBytes((String.format("%02x",partition.get())+"|"+"201703")))
					.addFamily(Bytes.toBytes("S"))
					.setFilter(filterList);
			
			callScan.setCacheBlocks(false);
			callScan.setCaching(0);
			callScan.setMaxVersions(1); //try and force the latest version only
			
			ResultScanner resultScanner = t1Table.getScanner(callScan);
			for (Result result:resultScanner ){
				context.getCounter(Counters.PERIODS).increment(1);
				if (!result.isEmpty()) {
					NavigableMap<byte[], byte[]> columnsKV = result.getFamilyMap(Bytes.toBytes("S"));
					for (byte[] column : columnsKV.keySet()) {
						Get call = new Get(column);
						call.addFamily(Bytes.toBytes("J"));
						Result callEvents = t1Table.get(call);
						String metric=null;
						try {
							metric = calculateCallMetrics(callEvents);
						} catch (org.json.simple.parser.ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						context.getCounter(Counters.CALLS).increment(1);
						if (metric != null){

							//WA - before put let's check if there is already a metric for it
							//TODO disable the block cache?
							Get existingMetricGet = new Get(column);
							existingMetricGet.addColumn(Bytes.toBytes("M"), Bytes.toBytes("M"));
							Result existingMetric = t1Table.get(existingMetricGet);
							if (!existingMetric.isEmpty() && metric.equals(Bytes.toString(existingMetric.getValue(Bytes.toBytes("M"), Bytes.toBytes("M"))))){
								LOGGER.info("Existing metric:"+Bytes.toString(existingMetric.getValue(Bytes.toBytes("M"), Bytes.toBytes("M"))));
								context.getCounter(Counters.NOT_RE_METERED).increment(1);

							} else {
								if (!metric.equals(Bytes.toString(existingMetric.getValue(Bytes.toBytes("M"), Bytes.toBytes("M"))))){
									context.getCounter(Counters.RE_METERED).increment(1);
								}
								
								//delete the existing value of the S 
								Delete deleteStatus=new Delete(result.getRow());
								deleteStatus.addColumn(Bytes.toBytes("S"), column); //mark call as deleted 	
								context.write(callstatus_key,deleteStatus);
								
								call_key.set(column);
								Put p=new Put(column);
								p.addColumn(Bytes.toBytes("M"), Bytes.toBytes("M"), Bytes.toBytes(metric));
								context.write(call_key, p);							
								LOGGER.info("Metric:"+p.toString());
								
								Put callStatus=new Put(result.getRow());
								callStatus.addColumn(Bytes.toBytes("S"), column, Bytes.toBytes("METERED")); //mark call as METERED so it will not be picked up again
								callstatus_key.set(result.getRow());
								context.write(callstatus_key,callStatus);
								LOGGER.info("Status="+callStatus.toString());
								context.getCounter(Counters.METERED).increment(1);
							}
						}
					}
				}
			}

			resultScanner.close();  // always close the ResultScanner!

		}

		public String calculateCallMetrics(Result result) throws org.json.simple.parser.ParseException{
			long startTimeStampL=0;
			long answerTimeStampL=0;
			long onholdTimeStampL=0;
			long dropTimeStampL=0;
			long hungupTimeStampL=0;
			long endTimeStampL=0;

			long lastTransitionTimeStampL=0;
			CallStatus lastEventType=CallStatus.valueOf("NONE");

			long ringTime=0;
			long talkTime=0; 
			long holdTime=0;
			long docTime=0;
			String id="";
			String agent="";
			String callCenter="";
			String topic="";
			String subtopic="";

			NavigableMap<byte[], byte[]> columnsKV = result.getFamilyMap(Bytes.toBytes("J"));


			//TODO - Hack together a metric JSON
			for (byte[] column : columnsKV.keySet()) {
				JSONObject json = (JSONObject) parser.parse(Bytes.toString(columnsKV.get(column)));
				String eventType=(String) json.get("type");
				if(eventType.equals(CallStatus.RINGING.toString())){
					//get start time from it
					id=(String) json.get("id");
					startTimeStampL=Long.parseLong((String) json.get("timestamp"));
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=startTimeStampL;	
				} else if(eventType.equals(CallStatus.DROPPED.toString())) {
					dropTimeStampL=Long.parseLong((String) json.get("timestamp"));
					if(lastEventType==CallStatus.RINGING) {
						ringTime=dropTimeStampL-lastTransitionTimeStampL;
					} else if (lastEventType==CallStatus.ONHOLD){
						holdTime=holdTime + answerTimeStampL -lastTransitionTimeStampL;
					}
					endTimeStampL=dropTimeStampL;
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=dropTimeStampL;
				}else if(eventType.equals(CallStatus.INPROGRESS.toString())) {
					answerTimeStampL=Long.parseLong((String) json.get("timestamp"));
					agent=(String) json.get("agent");
					callCenter=(String) json.get("callCenter");
					if(lastEventType==CallStatus.RINGING) {
						ringTime=answerTimeStampL-lastTransitionTimeStampL;
					} else if (lastEventType==CallStatus.ONHOLD){
						holdTime=holdTime + answerTimeStampL -lastTransitionTimeStampL;
					}
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=answerTimeStampL;
				}else if(eventType.equals(CallStatus.ONHOLD.toString())) {
					onholdTimeStampL=Long.parseLong((String) json.get("timestamp"));
					talkTime=talkTime+onholdTimeStampL-lastTransitionTimeStampL;
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=onholdTimeStampL;
				}else if(eventType.equals(CallStatus.FINISHED.toString())) {
					hungupTimeStampL=Long.parseLong((String) json.get("timestamp"));
					talkTime=talkTime+hungupTimeStampL-lastTransitionTimeStampL;
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=hungupTimeStampL;
				}else if(eventType.equals(CallStatus.COMPLETED.toString())) {
					endTimeStampL=Long.parseLong((String) json.get("timestamp"));
					topic=(String) json.get("topic");
					subtopic=(String) json.get("subtopic");
					docTime=docTime+endTimeStampL-lastTransitionTimeStampL;
					lastEventType=CallStatus.valueOf(eventType);
					lastTransitionTimeStampL=endTimeStampL;
				}
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

			return sb.toString();
		}
	}



	@Override
	public int run(String[] args) throws Exception {

		init() ;

		try {
			if (!parseOptions(args))
				return 1;
		} catch (IOException ex) {

			return 1;
		}

		// this should add the hbase configuration to the classpath on the
		// mappers.

		Configuration conf = getConf();
		HBaseConfiguration.merge(conf, HBaseConfiguration.create(conf));
		Job job = Job.getInstance(conf);
		TableMapReduceUtil.addDependencyJars(job);

		// security stuff
		if (System.getenv("HADOOP_TOKEN_FILE_LOCATION") != null) {
			conf.set("mapreduce.job.credentials.binary", System.getenv("HADOOP_TOKEN_FILE_LOCATION"));
		}
		TableMapReduceUtil.initCredentials(job);		

		RangeInputFormat.setNumberOfRows(job, 256);


		job.setJobName("CallMetricsMR");
		job.setJarByClass(CallMetricsMR.class);
		job.setMapperClass(CallMetricsMapper.class);
		job.setNumReduceTasks(0);
		/*
		Path outputDir = new Path(outputPath);
		if (outputDir.getFileSystem(getConf()).exists(outputDir)) {
			throw new IOException("Output directory " + outputDir + 
					" already exists.");
		}
		FileOutputFormat.setOutputPath(job, outputDir);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		 */
		job.setInputFormatClass(RangeInputFormat.class);

		TableMapReduceUtil.initTableReducerJob("call",
				IdentityTableReducer.class, job);

		return job.waitForCompletion(true) ? 0 : 1;

	}

	private void init() {

		options.addOption("o", "outputPath", true, "outputPath");
		options.addOption("n", "numRecords", true, "numRecords");

	}

	public boolean parseOptions(String args[]) throws ParseException, IOException {
		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CallMetricssMR", options, true);
			return false;
		}
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);	

		if (cmd.hasOption("o")) {
			outputPath = cmd.getOptionValue("o");
		} 

		if (cmd.hasOption("n")) {
			String records = cmd.getOptionValue("n");
			numRecords = Long.valueOf(records);
		}

		if (cmd.hasOption("i")) {
			inputPath = cmd.getOptionValue("i");
		} 

		return true;
	}



	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new CallMetricsMR(), args);
		System.exit(exitCode);
	}
}
