package com.sa.npopa.samples;

import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 
 * @author npopa
 * 
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ListStatus -conf hdfs-site.xml -conf core-site.xml -Dkerberos.principal=npopa -Dkerberos.keytab=/root/npopa.keytab hdfs://nameservice1/user/npopa
 * 
 */

public class ListStatus extends Configured implements Tool{
  
	private static String KRB_PRINCIPAL_PROP="kerberos.principal";
	private static String KRB_KEYTAB_PROP="kerberos.keytab";
	
  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    String uri = args[0];

    System.out.println(conf.get("hadoop.security.authentication"));
    UserGroupInformation.setConfiguration(conf);
    UserGroupInformation.loginUserFromKeytab(conf.get(KRB_PRINCIPAL_PROP), conf.get(KRB_KEYTAB_PROP));
    
    FileSystem fs = FileSystem.get(URI.create(uri), conf);
    Path[] paths = new Path[args.length];
    for (int i = 0; i < paths.length; i++) {
      paths[i] = new Path(args[i]);
    }
    
    FileStatus[] status = fs.listStatus(paths);
    Path[] listedPaths = FileUtil.stat2Paths(status);
    for (Path p : listedPaths) {
      System.out.println(p);
    }
    return 0;
  }
  
  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new ListStatus(), args);
    System.exit(exitCode);
  }

  
}

