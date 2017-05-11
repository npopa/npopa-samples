package com.sa.npopa.samples;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.security.auth.login.LoginContext;

/**
 * Example that uses <code>AuthenticatedURL</code>.
 */
public class HBaseRESTClient {

	private static String HBaseREST_REQ="https://npsec-cm.vpc.cloudera.com:20550/t1/exists";
	private static String HBaseREST_REQ1="https://npopa-cm.vpc.cloudera.com:20550/tip/KayYbHCt-RkbGcPdGOThNg2013-12-03xb6zEQCw9I-Gl0g06e1KsQ/J:R";
	private static String HTTPFS_SERVER="https://npopa-cm.vpc.cloudera.com:14000/webhdfs/v1/user/?op=GETFILESTATUS";

	public static void main(String[] args) {
		try {
			//if you want to run with the ticket cached then comment the jaas.conf line.
			System.setProperty("java.security.auth.login.config","/Users/npopa/Documents/workspace/HBaseRest/target/jaas.conf");
			System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
			
			
			//debug stuff
			System.setProperty("sun.security.krb5.debug", "true");
			//System.setProperty("javax.net.debug", "all");
			System.setProperty("sun.security.jgss.debug","true");


			AuthenticatedURL.Token token = new AuthenticatedURL.Token();

			URL url = new URL(HBaseREST_REQ);
			MyKerberosAuthenticator myKAuth=new MyKerberosAuthenticator();
			HttpURLConnection conn = new AuthenticatedURL(myKAuth).openConnection(url, token);
			System.out.println();
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
			
			URL url1 = new URL(HBaseREST_REQ1);
			HttpURLConnection conn1 = new AuthenticatedURL(myKAuth).openConnection(url1, token);
			System.out.println("Token value: " + token);
			System.out.println("Status code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
			System.out.println();
			if (conn1.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								conn1.getInputStream(), Charset.forName("UTF-8")));
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