package com.sa.npopa.samples;

import org.apache.http.HttpEntity;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class TestSPNEGO {
/*
	private static String ENDPOINT="https://npopa-cm.vpc.cloudera.com:20550/tip/KayYbHCt-RkbGcPdGOThNg2013-12-03xb6zEQCw9I-Gl0g06e1KsQ/J:R";
	public static void main(String[] args) {
		
	    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
	    System.setProperty("java.security.krb5.conf",
	            "/etc/krb5.conf");
	    System.setProperty("java.security.auth.login.config","/Users/npopa/Documents/workspace/HBaseRest/target/1.jaas");
	    
	    
	    System.setProperty("sun.security.krb5.debug", "true");
	    //System.setProperty("javax.net.debug", "all");
	  	System.setProperty("sun.security.jgss.debug","true");
	  	System.setProperty("java.security.debug","gssloginconfig,configfile,configparser,logincontext");
	  	System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");



	    Credentials jaasCredentials = new Credentials() {
	        public String getPassword() {
	            return null;
	        }

	        public Principal getUserPrincipal() {
	            return null;
	        }
	    };

	    CredentialsProvider credsProvider = new BasicCredentialsProvider();
	    credsProvider.setCredentials(new AuthScope(null, -1, null),
	            jaasCredentials);
	    Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder
	            .<AuthSchemeProvider> create().register(AuthSchemes.SPNEGO,
	                    new SPNegoSchemeFactory()).build();
	    CloseableHttpClient httpclient = HttpClients.custom()
	            .setDefaultAuthSchemeRegistry(authSchemeRegistry)
	            .setDefaultCredentialsProvider(credsProvider).build();

	    try {
	        HttpGet httpget = new HttpGet(ENDPOINT);
	        RequestLine requestLine = httpget.getRequestLine();
	        CloseableHttpResponse response = httpclient.execute(httpget);
	        try {
	            StatusLine status = response.getStatusLine();
	            HttpEntity entity = response.getEntity();
	            if (entity != null) {
	            }
	            EntityUtils.consume(entity);
	        } finally {
	            response.close();
	        }
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            httpclient.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	*/
}
