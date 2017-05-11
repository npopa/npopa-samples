package com.sa.npopa.samples.hbase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mob.MobConstants;
import org.apache.hadoop.hbase.mob.MobUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.*;

public class CallStats extends Configured implements Tool {

	private static String tableName = "call";
	private static String family = "J";

	private Configuration conf = HBaseConfiguration.create();

	@Override
	public int run(String[] args) throws Exception {
		
		// Setting up the HBase configuration
		conf.addResource("/Users/npopa/Documents/gist/hbase-site.xml");
		
		// Point to the krb5.conf file.
		//System.setProperty("java.security.krb5.conf", "src/main/resources/krb5.conf");
		//System.setProperty("sun.security.krb5.debug", "true");
		 
		// Override these values by setting -DkerberosPrincipal and/or -DkerberosKeytab
		String principal = System.getProperty("kerberosPrincipal", "hb575072@AD.SEC.CLOUDERA.COM");
		String keytabLocation = System.getProperty("kerberosKeytab", "/Users/npopa/Documents/gist/keytabs/hb575072.keytab");

		UserGroupInformation.setConfiguration(conf);
		UserGroupInformation.loginUserFromKeytab(principal, keytabLocation);
		System.out.println("Logged in.");
		
		Connection connection = ConnectionFactory.createConnection(conf);
		Table table = connection.getTable(TableName.valueOf(tableName));
		
		System.out.println("Connection created.");		

		List<Get> gets = new LinkedList<Get>();
		for(int i=0;i<=0xff;i++){
			//System.out.println(String.format("%02x", i));
			Get get = new Get((String.format("%02x", i)+"|"+"2017021800").getBytes());
			gets.add(get);
		}
		System.out.println("Getting from table "+tableName);
		Result results[] =table.get(gets);
		System.out.println(results.length);
		for (Result result: results){
			System.out.println(result.toString());		
		}


		return 0;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new CallStats(), args);
		System.exit(exitCode);
	}
}
