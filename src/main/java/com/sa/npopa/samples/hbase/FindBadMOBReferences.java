/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sa.npopa.samples.hbase;

/**
 * kinit hb580539
 * export HBASE_CLASSPATH=npopa-samples-0.0.1-SNAPSHOT.jar
 * hbase com.sa.npopa.samples.hbase.FindBadMOBReferences testMOB
 * 
 */

import java.io.IOException;
import org.apache.hadoop.io.Text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.KeyValueUtil;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mob.MobConstants;
import org.apache.hadoop.hbase.mob.MobUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class FindBadMOBReferences extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(FindBadMOBReferences.class);

	/** Name of this 'program'. */
	static final String NAME = "FindBadMOBReferences";

	private final static String JOB_NAME_CONF_KEY = "mapreduce.job.name";

	static class FindBadMOBReferencesMapper extends TableMapper<Text, Text> {

		/** Counter enumeration to count the actual rows. */
		public static enum Counters {
			ROWS, MOB_ROWS
		}

		@Override
		public void map(ImmutableBytesWritable row, Result values, Context context)
				throws IOException, InterruptedException {
			// count all rows
			context.getCounter(Counters.ROWS).increment(1);
			
			if (values == null) {
				return;
			}
			Cell[] cells = values.rawCells();
			if (cells == null || cells.length == 0) {
				return;
			}
			for (Cell c : cells) {
				if (MobUtils.hasValidMobRefCellValue(c)) {
					String fileName = MobUtils.getMobFileName(c);
					context.getCounter(Counters.MOB_ROWS).increment(1);
					context.write(new Text(fileName), new Text(Bytes.toString(row.get())));
				}
			}

		}
	}

	static class FindBadMOBReferencesReducer extends Reducer<Text, Text, Text,Text> {

		/** Counter enumeration to count the actual rows. */
		public static enum Counters {
			ROWS, MOB_ROWS
		}

		@Override
	    protected void reduce(Text key, Iterable<Text> values,
	    	      Context context) throws IOException, InterruptedException {
	    	      int count = 0;
	    	      for (Text rowkey : values) count++; 
	    	      context.write(key, new Text(String.valueOf(count)));
	    	    }
	}
	
	
	public static Job createSubmittableJob(Configuration conf, String[] args) throws IOException {
		String tableName = args[0];

		Job job = Job.getInstance(conf, conf.get(JOB_NAME_CONF_KEY, NAME + "_" + tableName));
		job.setJarByClass(FindBadMOBReferences.class);

		Scan scan = new Scan();
		scan.setCacheBlocks(false);
		scan.setBatch(10);
		scan.setAttribute(MobConstants.MOB_SCAN_RAW, Bytes.toBytes(Boolean.TRUE));
		scan.setAttribute(MobConstants.MOB_SCAN_REF_ONLY, Bytes.toBytes(Boolean.TRUE));
		scan.addFamily(Bytes.toBytes("J"));
		//scan.setRowPrefixFilter(Bytes.toBytes("a00"));

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		// job.setOutputFormatClass(NullOutputFormat.class);
		TableMapReduceUtil.initTableMapperJob(tableName, scan, FindBadMOBReferencesMapper.class, Text.class, Text.class,
				job);
		
		//job.setNumReduceTasks(0);
		job.setReducerClass(FindBadMOBReferencesReducer.class);
	    job.setOutputKeyClass(Text.class); 
	    job.setOutputValueClass(Text.class);
	    job.setNumReduceTasks(1);
		
		FileOutputFormat.setOutputPath(job, new Path("/tmp/out"));
		return job;
	}

	/*
	 * @param errorMessage Can attach a message when error occurs.
	 */
	private static void printUsage(String errorMessage) {
		System.err.println("ERROR: " + errorMessage);
		printUsage();
	}

	private static void printUsage() {
		System.err.println("Usage: FindBadMOBReferencesMapper [options] <tablename> ");
		System.err.println("For performance consider the following options:\n" + "-Dhbase.client.scanner.caching=100\n"
				+ "-Dmapreduce.map.speculative=false");
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 1) {
			printUsage("Wrong number of parameters: " + args.length);
			return -1;
		}
		Job job = createSubmittableJob(getConf(), args);
		if (job == null) {
			return -1;
		}
		boolean success = job.waitForCompletion(true);
		return (success ? 0 : 1);
	}

	public static void main(String[] args) throws Exception {
		int errCode = ToolRunner.run(HBaseConfiguration.create(), new FindBadMOBReferences(), args);
		System.exit(errCode);
	}

}