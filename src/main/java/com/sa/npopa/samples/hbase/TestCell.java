package com.sa.npopa.samples.hbase;

import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mob.MobConstants;
import org.apache.hadoop.hbase.mob.MobUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.*;

public class TestCell extends Configured implements Tool {

	private static String tableName = "table1";
	private static String family = "J";
	private static String row = "feae3f501e5e3a312ff2999f9789915c20160801";

	private Configuration conf = HBaseConfiguration.create();

	@Override
	public int run(String[] args) throws Exception {
		Connection connection = ConnectionFactory.createConnection(conf);
		Table table = connection.getTable(TableName.valueOf(tableName));

		System.out.println("Scanning table #1...");
		// vv ScanExample
		Scan scan1 = new Scan();

		scan1.setRowPrefixFilter(Bytes.toBytes("10003"));
	    scan1.setAttribute(MobConstants.MOB_SCAN_RAW, Bytes.toBytes(Boolean.TRUE)); //this allows missing MOBs to be scanned
	    scan1.setAttribute(MobConstants.MOB_SCAN_REF_ONLY, Bytes.toBytes(Boolean.TRUE)); //this should only get the MOBs (missing or not)
	    
		ResultScanner scanner1 = table.getScanner(scan1);
		for (Result values : scanner1) {
			System.out.println("Values:"+values);
			if (values == null) {
				return -1;
			}
			Cell[] cells = values.rawCells();
			if (cells == null || cells.length == 0) {
				return -1;
			}
			for (Cell c : cells) {
				System.out.println("Cell:"+c);				
				if (MobUtils.hasValidMobRefCellValue(c)) {
					String fileName = MobUtils.getMobFileName(c);
					System.out.println("File:"+fileName);	
				}
			}
		}
		scanner1.close(); 
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new TestCell(), args);
		System.exit(exitCode);
	}
}
