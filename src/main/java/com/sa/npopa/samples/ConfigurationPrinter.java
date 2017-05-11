package com.sa.npopa.samples;

import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;

/**
 * 
 * @author npopa
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter -conf myConf.xml -DmyCommandLineProp=myCommandLineValue
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter -conf myConf.xml -Dmy.new.property=myValue1
 */


public class ConfigurationPrinter extends Configured implements Tool {
  

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    for (Entry<String, String> entry: conf) {
      System.out.printf("%s=%s\n", entry.getKey(), entry.getValue());
    }
    return 0;
  }
  
  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new ConfigurationPrinter(), args);
    System.exit(exitCode);
  }
}
