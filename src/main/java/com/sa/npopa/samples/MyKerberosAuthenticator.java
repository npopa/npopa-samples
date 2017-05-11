package com.sa.npopa.samples;

import static org.apache.hadoop.util.PlatformName.IBM_JAVA;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hadoop.security.authentication.client.Authenticator;
import org.apache.hadoop.security.authentication.client.ConnectionConfigurator;
import org.apache.hadoop.security.authentication.client.KerberosAuthenticator;

import org.apache.hadoop.security.authentication.util.KerberosUtil;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example that uses <code>AuthenticatedURL</code>.
 */
public class MyKerberosAuthenticator extends KerberosAuthenticator {
	  private static Logger LOG = LoggerFactory.getLogger(
			  MyKerberosAuthenticator.class);	
	  private static boolean KEYTAB=false;
	  private URL url;
	  private HttpURLConnection conn;
	  private Base64 base64;
	  private ConnectionConfigurator connConfigurator;
	  /**
	   * HTTP header used by the SPNEGO server endpoint during an authentication sequence.
	   */
	  public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	  /**
	   * HTTP header used by the SPNEGO client endpoint during an authentication sequence.
	   */
	  public static final String AUTHORIZATION = "Authorization";

	  /**
	   * HTTP header prefix used by the SPNEGO client/server endpoints during an authentication sequence.
	   */
	  public static final String NEGOTIATE = "Negotiate";

	  //this is OPTIONS in regular authenticator but HBASE Rest server rejects it and does not attempt to negotiate
	  
	  private static final String AUTH_HTTP_METHOD = "GET"; 
	  
	  /*
	   * Defines the Kerberos configuration that will be used to obtain the Kerberos principal from the
	   * Kerberos cache.
	   */
	   private static class KerberosConfiguration extends Configuration {

	     private static final String OS_LOGIN_MODULE_NAME;
	     private static final boolean windows = System.getProperty("os.name").startsWith("Windows");
	     private static final boolean is64Bit = System.getProperty("os.arch").contains("64");
	     private static final boolean aix = System.getProperty("os.name").equals("AIX");

	     /* Return the OS login module class name */
	     private static String getOSLoginModuleName() {
	       if (IBM_JAVA) {
	         if (windows) {
	           return is64Bit ? "com.ibm.security.auth.module.Win64LoginModule"
	               : "com.ibm.security.auth.module.NTLoginModule";
	         } else if (aix) {
	           return is64Bit ? "com.ibm.security.auth.module.AIX64LoginModule"
	               : "com.ibm.security.auth.module.AIXLoginModule";
	         } else {
	           return "com.ibm.security.auth.module.LinuxLoginModule";
	         }
	       } else {
	         return windows ? "com.sun.security.auth.module.NTLoginModule"
	             : "com.sun.security.auth.module.UnixLoginModule";
	       }
	     }

	     static {
	       OS_LOGIN_MODULE_NAME = getOSLoginModuleName();
	     }

	     private static final AppConfigurationEntry OS_SPECIFIC_LOGIN =
	       new AppConfigurationEntry(OS_LOGIN_MODULE_NAME,
	                                 AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
	                                 new HashMap<String, String>());

	     private static final Map<String, String> USER_KERBEROS_OPTIONS = new HashMap<String, String>();

	     static {
	       String ticketCache = System.getenv("KRB5CCNAME");
/*	       if (KEYTAB) {
	    	   USER_KERBEROS_OPTIONS.put("useKeyTab","true");
	    	   USER_KERBEROS_OPTIONS.put("storeKey","true");	 
		       USER_KERBEROS_OPTIONS.put("doNotPrompt", "true");
		       USER_KERBEROS_OPTIONS.put("keyTab", "npopa.keytab");	
		       USER_KERBEROS_OPTIONS.put("principal","npopa@AD.SEC.CLOUDERA.COM");
	       } else {*/
	       if (IBM_JAVA) {
	         USER_KERBEROS_OPTIONS.put("useDefaultCcache", "true");
	       } else {
	         USER_KERBEROS_OPTIONS.put("doNotPrompt", "true");
	         USER_KERBEROS_OPTIONS.put("useTicketCache", "true");
	       }
	       if (ticketCache != null) {
	         if (IBM_JAVA) {
	           // The first value searched when "useDefaultCcache" is used.
	           System.setProperty("KRB5CCNAME", ticketCache);
	         } else {
	           USER_KERBEROS_OPTIONS.put("ticketCache", ticketCache);
	         }
	       //}

	       USER_KERBEROS_OPTIONS.put("renewTGT", "true");
	       }
	     }

	     private static final AppConfigurationEntry USER_KERBEROS_LOGIN =
	       new AppConfigurationEntry(KerberosUtil.getKrb5LoginModuleName(),
	                                 AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL,
	                                 USER_KERBEROS_OPTIONS);

	     private static final AppConfigurationEntry[] USER_KERBEROS_CONF =
	       new AppConfigurationEntry[]{OS_SPECIFIC_LOGIN, USER_KERBEROS_LOGIN};

	     @Override
	     public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
	       return USER_KERBEROS_CONF;
	     }
	   }	  
	  
	  @Override
	  public void authenticate(URL url, AuthenticatedURL.Token token)
	    throws IOException, AuthenticationException {
	    if (!token.isSet()) {
	      this.url = url;
	      base64 = new Base64(0);
	      conn = (HttpURLConnection) url.openConnection();
	      if (connConfigurator != null) {
	        conn = connConfigurator.configure(conn);
	      }
	      conn.setRequestMethod(AUTH_HTTP_METHOD);
	      conn.connect();
	      
	      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        LOG.debug("JDK performed authentication on our behalf.");
	        // If the JDK already did the SPNEGO back-and-forth for
	        // us, just pull out the token.
	        AuthenticatedURL.extractToken(conn, token);
	        return;
	      } else if (isNegotiate()) {
	        LOG.debug("Performing our own SPNEGO sequence.");
	        doSpnegoSequence(token);
	      } else {
	        LOG.debug("Using fallback authenticator sequence.");
	        Authenticator auth = getFallBackAuthenticator();
	        // Make sure that the fall back authenticator have the same
	        // ConnectionConfigurator, since the method might be overridden.
	        // Otherwise the fall back authenticator might not have the information
	        // to make the connection (e.g., SSL certificates)
	        auth.setConnectionConfigurator(connConfigurator);
	        auth.authenticate(url, token);
	      }
	    }
	  }
	  /*
	   * Indicates if the response is starting a SPNEGO negotiation.
	   */
	   private boolean isNegotiate() throws IOException {
	     boolean negotiate = false;
	     if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
	       String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
	       negotiate = authHeader != null && authHeader.trim().startsWith(NEGOTIATE);
	     }
	     return negotiate;
	   }

	   /**
	    * Implements the SPNEGO authentication sequence interaction using the current default principal
	    * in the Kerberos cache (normally set via kinit).
	    *
	    * @param token the authentication token being used for the user.
	    *
	    * @throws IOException if an IO error occurred.
	    * @throws AuthenticationException if an authentication error occurred.
	    */
	   private void doSpnegoSequence(AuthenticatedURL.Token token) throws IOException, AuthenticationException {
	     try {
	       AccessControlContext context = AccessController.getContext();
	       Subject subject = Subject.getSubject(context);
	       if (subject == null) {
	         LOG.debug("No subject in context, logging in");
	         subject = new Subject();
	         LoginContext login = new LoginContext("", subject,
	             null, new KerberosConfiguration());
	         /*LoginContext login = new LoginContext("Client");
	         login.login();
	         subject=login.getSubject();*/
	         System.out.println(subject);
	       }

	       if (LOG.isDebugEnabled()) {
	         LOG.debug("Using subject: " + subject);
	       }
	       Subject.doAs(subject, new PrivilegedExceptionAction<Void>() {

	         @Override
	         public Void run() throws Exception {
	           GSSContext gssContext = null;
	           try {
	             GSSManager gssManager = GSSManager.getInstance();
	             String servicePrincipal = KerberosUtil.getServicePrincipal("HTTP",
	                 MyKerberosAuthenticator.this.url.getHost());
	             Oid oid = KerberosUtil.getOidInstance("NT_GSS_KRB5_PRINCIPAL");
	             GSSName serviceName = gssManager.createName(servicePrincipal,
	                                                         oid);
	             oid = KerberosUtil.getOidInstance("GSS_KRB5_MECH_OID");
	             gssContext = gssManager.createContext(serviceName, oid, null,
	                                                   GSSContext.DEFAULT_LIFETIME);
	             gssContext.requestCredDeleg(true);
	             gssContext.requestMutualAuth(true);

	             byte[] inToken = new byte[0];
	             byte[] outToken;
	             boolean established = false;

	             // Loop while the context is still not established
	             while (!established) {
	               outToken = gssContext.initSecContext(inToken, 0, inToken.length);
	               if (outToken != null) {
	                 sendToken(outToken);
	               }

	               if (!gssContext.isEstablished()) {
	                 inToken = readToken();
	               } else {
	                 established = true;
	               }
	             }
	           } finally {
	             if (gssContext != null) {
	               gssContext.dispose();
	               gssContext = null;
	             }
	           }
	           return null;
	         }
	       });
	     } catch (PrivilegedActionException ex) {
	       throw new AuthenticationException(ex.getException());
	     } catch (LoginException ex) {
	       throw new AuthenticationException(ex);
	     }
	     AuthenticatedURL.extractToken(conn, token);
	   }
	   /*
	    * Sends the Kerberos token to the server.
	    */
	    private void sendToken(byte[] outToken) throws IOException {
	      String token = base64.encodeToString(outToken);
	      conn = (HttpURLConnection) url.openConnection();
	      if (connConfigurator != null) {
	        conn = connConfigurator.configure(conn);
	      }
	      conn.setRequestMethod(AUTH_HTTP_METHOD);
	      conn.setRequestProperty(AUTHORIZATION, NEGOTIATE + " " + token);
	      conn.connect();
	    }

	    /*
	    * Retrieves the Kerberos token returned by the server.
	    */
	    private byte[] readToken() throws IOException, AuthenticationException {
	      int status = conn.getResponseCode();
	      if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_UNAUTHORIZED) {
	        String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
	        if (authHeader == null || !authHeader.trim().startsWith(NEGOTIATE)) {
	          throw new AuthenticationException("Invalid SPNEGO sequence, '" + WWW_AUTHENTICATE +
	                                            "' header incorrect: " + authHeader);
	        }
	        String negotiation = authHeader.trim().substring((NEGOTIATE + " ").length()).trim();
	        return base64.decode(negotiation);
	      }
	      throw new AuthenticationException("Invalid SPNEGO sequence, status code: " + status);
	    }	  
}