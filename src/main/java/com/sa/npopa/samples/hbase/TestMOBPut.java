package com.sa.npopa.samples.hbase;

import java.util.Random;
import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mob.MobConstants;
import org.apache.hadoop.hbase.mob.MobUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.*;

import com.sa.npopa.samples.util.SampleMessage;

/**
 * kinit hb580539 
 * export HBASE_CLASSPATH=npopa-samples-0.0.1-SNAPSHOT.jar 
 * hbase com.sa.npopa.samples.hbase.TestMOBPut
 * 
 * echo "disable 'testMOB';drop 'testMOB'; create 'testMOB',{NAME => 'J', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', VERSIONS => '1', IS_MOB => true, MOB_THRESHOLD => 5000}, {NUMREGIONS => 16, SPLITALGO => 'HexStringSplit'};grant 'flume','RWCA','testMOB'"|hbase shell
 * 
 */

public class TestMOBPut extends Configured implements Tool {

	private static String tableName = "testMOB";
	private static String family = "M";

	private Configuration conf = HBaseConfiguration.create();

	@Override
	public int run(String[] args) throws Exception {
		Connection connection = ConnectionFactory.createConnection(conf);
		Table table = connection.getTable(TableName.valueOf(tableName));
		Admin admin = connection.getAdmin();
		System.out.println("Pushing a MOB to table #1...");
		Random rand = new Random();
		long ts = 1447286400 * 1000L;

		for (int i = 1; i <= 30; i++) // generate 30 days of mobs
		{

			for (int k = 1; k <= 6; k++) { // 6 flushes to generate ~6 MOBs files per day  ~ 1000 mobs per day

				for (int j = 1; j < 900 + rand.nextInt(200); j++) {
					SampleMessage msg = new SampleMessage(1024 + rand.nextInt(1024 * 10));
					Put put = new Put(Bytes.toBytes(msg.getKey()));
					put.addColumn(Bytes.toBytes("J"), Bytes.toBytes("R"), ts, Bytes.toBytes(msg.getMessage()));
					table.put(put);
				}
				admin.flush(TableName.valueOf(tableName));
				ts += 60 * 60 * 4 * 1000L; // increment TS by 4 hours
			}
		}

		table.close();
		connection.close();
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new TestMOBPut(), args);
		System.exit(exitCode);
	}
}
