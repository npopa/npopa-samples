package com.sa.npopa.samples;

import java.sql.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
/*
export CLASSPATH=/opt/cloudera/parcels/CDH/lib/hive/lib/*
export CLASSPATH=`hadoop classpath`:$CLASSPATH:.:npopa-samples-0.0.1-SNAPSHOT.jar:/root/zeppelin-0.7.0-bin-all/lib/*
java  -cp "./target/*:$CLASSPATH:." com.sa.npopa.samples.CDHHiveConnect \
-Djava.security.auth.login.config=/root/jaas.conf \
      "jdbc:hive2://npopa-4.vpc.cloudera.com:10000/default;AuthMech=1;KrbAuthType=1;KrbRealm=AD.SEC.CLOUDERA.COM;KrbHostFQDN=npopa-4.vpc.cloudera.com;KrbServiceName=hive;SSL=1;SSLTrustStore=/opt/cloudera/security/jks/truststore.jks; SSLTrustStorePwd=cloudera;"

 * 
 * 
 * -Dsun.security.krb5.debug=true -Djava.security.debug=gssloginconfig,configfile,configparser,logincontext
 */

public class CDHHiveConnect extends Configured implements Tool {


	static String JDBCDriver = "com.cloudera.hive.jdbc41.HS2Driver";
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		String uri = args[0];
		System.out.println(conf.get("hadoop.security.authentication"));

		try {
			Class.forName(JDBCDriver);
			Connection con = DriverManager.getConnection(uri);
			String query = "select * from earthquake limit 10";
			Statement stmt = con.createStatement();
			System.out.println("Executing Query...");
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String earthquake_date = rs.getString("earthquake_date"); 
				String latitude = rs.getString("latitude"); 
				String longitude = rs.getString("longitude");
				String depth= rs.getString("depth");
				String magnitude= rs.getString("magnitude");
				String magtype= rs.getString("magtype");
				System.out.printf("%20s%20s%20s%20s%20s\r\n", earthquake_date, latitude, longitude,depth,magnitude,magtype);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new CDHHiveConnect(), args);
		System.exit(exitCode);
	}

}


