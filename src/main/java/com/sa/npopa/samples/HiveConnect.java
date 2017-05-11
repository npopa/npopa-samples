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
export CLASSPATH=`hadoop classpath`:$CLASSPATH:.:npopa-samples-0.0.1-SNAPSHOT.jar
java  -cp "./target/*:$CLASSPATH:." com.sa.npopa.samples.HiveConnect -conf hdfs-site.xml -conf core-site.xml -Dkerberos.principal=npopa -Dkerberos.keytab=/root/npopa.keytab "jdbc:hive2://npsec-mst-1.vpc.cloudera.com:10000/default;principal=hive/_HOST@AD.SEC.CLOUDERA.COM"

 * 
 * 
 * -Dsun.security.krb5.debug=true -Djava.security.debug=gssloginconfig,configfile,configparser,logincontext
 */

public class HiveConnect extends Configured implements Tool {

	private static String KRB_PRINCIPAL_PROP = "kerberos.principal";
	private static String KRB_KEYTAB_PROP = "kerberos.keytab";
	static String JDBCDriver = "org.apache.hive.jdbc.HiveDriver";
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		String uri = args[0];

		System.out.println(conf.get("hadoop.security.authentication"));
		UserGroupInformation.setConfiguration(conf);
		try {
			UserGroupInformation.loginUserFromKeytab(conf.get(KRB_PRINCIPAL_PROP), conf.get(KRB_KEYTAB_PROP));
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		int exitCode = ToolRunner.run(new HiveConnect(), args);
		System.exit(exitCode);
	}

}
