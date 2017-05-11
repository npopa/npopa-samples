package com.sa.npopa.samples;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Example that uses <code>AuthenticatedURL</code>.

npopa-MBP:tmp npopa$ cat /Users/npopa/Documents/workspace/HBaseRest/target/jaas.conf
com.sun.security.jgss.krb5.initiate {
com.sun.security.auth.module.Krb5LoginModule required
doNotPrompt=true
principal=npopa
useKeyTab=true
keyTab="/Users/npopa/Documents/workspace/HBaseRest/target/npopa.keytab"
storeKey=true
debug=true;
};
com.sun.security.jgss.krb5.accept {
com.sun.security.auth.module.Krb5LoginModule required
doNotPrompt=true
principal=npopa
useKeyTab=true
keyTab="/Users/npopa/Documents/workspace/HBaseRest/target/npopa.keytab"
storeKey=true
debug=true;
};


 */
public class HttpFSRESTClient {


	private static String HTTPFS_SERVER="https://npopa-2.vpc.cloudera.com:14000/webhdfs/v1/user/?op=GETFILESTATUS";

	public static void main(String[] args) {
		try {
			//if you want to run with the ticket cached then comment the jaas.conf line.
			System.setProperty("java.security.auth.login.config","/Users/npopa/Documents/workspace/HBaseRest/target/jaas.conf");
			System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
			
			
			//debug stuff
			//System.setProperty("sun.security.krb5.debug", "true");
			//System.setProperty("sun.security.jgss.debug","true");
		   
			System.setProperty("javax.net.ssl.trustStore", "/tmp/truststore.jks");
			System.setProperty("javax.net.ssl.trustStorePassword","cloudera");

			AuthenticatedURL.Token token = new AuthenticatedURL.Token();

			URL url = new URL(HTTPFS_SERVER);
			MyKerberosAuthenticator myKAuth=new MyKerberosAuthenticator();
			HttpURLConnection conn = new AuthenticatedURL(myKAuth).openConnection(url, token);
			System.out.println("");
			System.out.println("Token value: " + token);
			System.out.println("Status code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
			System.out.println();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								conn.getInputStream(), Charset.forName("UTF-8")));
				String line = reader.readLine();
				while (line != null) {
					System.out.println(line);
					line = reader.readLine();
				}
				reader.close();
			}
			System.out.println();
					
		}
		catch (Exception ex) {
			System.err.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
	}
}