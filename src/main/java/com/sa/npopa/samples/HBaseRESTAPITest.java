package com.sa.npopa.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sa.npopa.samples.hbase.rest.client.Client;
import com.sa.npopa.samples.hbase.rest.client.Cluster;
import com.sa.npopa.samples.hbase.rest.client.RemoteHTable;

public class HBaseRESTAPITest {

	

	static final String TABLE = "tip";
	static final byte[] _CF = Bytes.toBytes("J");
	static final byte[] _CQ = Bytes.toBytes("R");

	static final String REST_SERVER = "npopa-cm.vpc.cloudera.com";

	public static void main(String[] args) throws IOException {
	    System.setProperty("java.security.auth.login.config","/Users/npopa/Documents/workspace/HBaseRest/target/jaas.conf");
	    System.setProperty("sun.security.krb5.debug", "true");
	    //System.setProperty("javax.net.debug", "all");
	  	System.setProperty("sun.security.jgss.debug","true");
	  	System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
	  	System.setProperty("sun.security.ssl.allowUnsafeRenegotiation","true");

	  	
		String output_file_name = "abc";

		String key = "KayYbHCt-RkbGcPdGOThNg2013-12-03xb6zEQCw9I-Gl0g06e1KsQ";


		Configuration conf = HBaseConfiguration.create();

		Cluster cluster = new Cluster();
		cluster.add(REST_SERVER, 20550);
		Client client = new Client(cluster,true);
		int myattempt=client.getHttpClient().executeMethod(new GetMethod("https://npopa-cm.vpc.cloudera.com:20550"));
		System.out.println("Response="+myattempt);
		
		
		client.addExtraHeader("Authenticate", "Negotiate");

		
		RemoteHTable table = new RemoteHTable(client, TABLE);




		Get get = new Get(Bytes.toBytes(key));
		get.addColumn(_CF, _CQ);

		Result result = table.get(get);
		byte[] b = result.getValue(_CF, _CQ);

		File file = new File(output_file_name);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(b);
		outputStream.flush();
		outputStream.close();


		table.close();

	}
}
