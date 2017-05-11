package com.sa.npopa.samples.hbase;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
disable 't1'; drop 't1'
create 't1', {NAME => 'f1', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', VERSIONS => '1', TTL => '33696000' }, {NUMREGIONS => 32, SPLITALGO => 'HexStringSplit'}

kinit hb580539
export HBASE_CLASSPATH=npopa-samples-0.0.1-SNAPSHOT.jar 
export HADOOP_CLASSPATH=`hbase classpath`
hdfs dfs -rm -r -skipTrash  /tmp/myhfiles
hadoop jar npopa-samples-0.0.1-SNAPSHOT.jar com.sa.npopa.samples.hbase.myBulkLoad -libjars /opt/cloudera/parcels/CDH/jars/json-simple-1.1.1.jar \
   --input /tmp/mybulkload/col1 \
   --output /tmp/myhfiles \
   --tableName t1 \
   --columnFamily f1 \
   --columnQualifier c1 \
   --rowkeyAttribute id

hadoop fs -ls /tmp/myhfiles/

hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /tmp/myhfiles t1
hbase org.apache.hadoop.hbase.mapreduce.RowCounter t1

 */


public class myBulkLoad_old extends Configured implements Tool {

  private Options options = new Options();
  private static final Log LOG = LogFactory.getLog(myBulkLoad_old.class);
	private static String table_name;// = "t1";
	private static String family;// = "f1";
	private static String column;// ="c1";
	private static String rowkey_attribute;// ="id";
	private static String inputPath;// ="id";
	private static String outputPath;// ="id";
	

	
	public myBulkLoad_old() {
		super();
		init();
	}

	public myBulkLoad_old(Configuration conf) {
		super(conf);
		init();
	}

	
	static class myBulkLoadMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Cell> {

	  
	  public static  byte[] CF ;
	  public static  byte[] COL ;
	  public static String rowkeyAttribute;
	  public static final ImmutableBytesWritable rowKey = new ImmutableBytesWritable();
	  private JSONParser parser = new JSONParser();
	    
    
    
    protected void setup (Context context) {
    	Configuration conf = context.getConfiguration();
    	CF = Bytes.toBytes(conf.get("myBulkLoad.columnFamily"));
    	COL = Bytes.toBytes(conf.get("myBulkLoad.columnQualifier"));  
    	rowkeyAttribute = conf.get("myBulkLoad.rowkeyAttribute");

    }
    protected void map(LongWritable offset, Text line, Context context) throws IOException,
        InterruptedException {

        try {
            JSONObject json = (JSONObject) parser.parse(line.toString());
            String id = (String) json.get(rowkeyAttribute);
            rowKey.set(Bytes.toBytes(id)); 
            KeyValue kv = new KeyValue(rowKey.get(), CF, COL, line.copyBytes()); 

            context.write(rowKey, kv);
          } catch (Exception e) {
            e.printStackTrace();
          }
 
    }
    
    
  }


  public static void printUsage(Tool tool, String extraArgsUsage) {
    System.err.printf("Usage: %s [genericOptions] %s\n\n", tool.getClass().getSimpleName(),
      extraArgsUsage);
    GenericOptionsParser.printGenericCommandUsage(System.err);
  }

	private void init() {

		options.addOption("t", "tableName", true, "table name ie. table1");
		options.addOption("f", "columnFamily", true, "column family ie. J");
		options.addOption("c", "columnQualifier", true, "column qualifier ie. C");
		options.addOption("k", "rowkeyAttribute", true, "Json attribute to be used as rowkey ie. id");
		options.addOption("i", "input", true, "input file/folder in hdfs");
		options.addOption("o", "output", true, "ouptput path in HDFS");
		}

	public boolean parseOptions(String args[]) throws ParseException, IOException {
		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("myBulkLoad", options, true);
			return false;
		}
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("t")) {
			table_name = cmd.getOptionValue("t");
		}
		if (cmd.hasOption("f")) {
			family = cmd.getOptionValue("f");
		}
		if (cmd.hasOption("c")) {
			column = cmd.getOptionValue("c");
		}
		if (cmd.hasOption("k")) {
			rowkey_attribute = cmd.getOptionValue("k");
		}
		if (cmd.hasOption("i")) {
			inputPath = cmd.getOptionValue("i");
		}
		if (cmd.hasOption("o")) {
			outputPath = cmd.getOptionValue("o");
		}

		return true;
	}
  

  public int run(String[] args) throws Exception {
   
		try {
			if (!parseOptions(args))
				return 1;
		} catch (IOException ex) {
			LOG.error("Error parsing command-line options", ex);
			return 1;
		} catch (ParseException ex) {
			LOG.error("Error parsing command-line options", ex);
			return 1;
		}
	  
	Configuration conf = getConf();
	HBaseConfiguration.merge(conf, HBaseConfiguration.create(conf));
	
	//pass parameters to map/reduce if needed
	conf.set("myBulkLoad.tableName", table_name);
	conf.set("myBulkLoad.columnFamily", family);
	conf.set("myBulkLoad.columnQualifier", column);
	conf.set("myBulkLoad.rowkeyAttribute", rowkey_attribute);
	
   Connection connection = ConnectionFactory.createConnection(conf);
   final TableName tableName = TableName.valueOf(table_name);
   Table table = connection.getTable(tableName);
   
   Job job = Job.getInstance(conf, "Import from file " + inputPath +
		      " into table " + table_name +
		      " using intermediate path " + outputPath
		      );
    job.setJarByClass(myBulkLoad_old.class);
    HFileOutputFormat2.configureIncrementalLoad(job, table,
            connection.getRegionLocator(tableName)); 
    
    job.setMapperClass(myBulkLoadMapper.class);   
    job.setMapOutputKeyClass(ImmutableBytesWritable.class);
    job.setMapOutputValueClass(KeyValue.class); 

    HFileOutputFormat2.setOutputPath(job, new Path(outputPath));  
    FileInputFormat.addInputPath(job, new Path(inputPath));

   int result=0;
    //return job.waitForCompletion(true) ? 0 : 1;
   job.waitForCompletion(true);
    
    if (job.isSuccessful()) {
    	result=0;
        // if immediate importing the generated HFiles into a HBase table is needed
        //LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
        //loader.doBulkLoad(new Path(outputPath), new HTable(conf,table_name));
    } else {
        result = -1;
    }
    
    return result;
  }

  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new myBulkLoad_old(), args);
    System.exit(exitCode);
  }
}
