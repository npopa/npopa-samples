package com.sa.npopa.samples;

import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.KeyValue.Type;

import org.apache.hadoop.hbase.Tag;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFile.Reader;
import org.apache.hadoop.hbase.io.hfile.HFile.Writer;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileContextBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;

/**
 * 
 * @author npopa
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter -conf myConf.xml -DmyCommandLineProp=myCommandLineValue
 * java  -cp "./target/*:$CLASSPATH:." com/sa/npopa/samples/ConfigurationPrinter -conf myConf.xml -Dmy.new.property=myValue1
 */


public class EmptyHBaseFile extends Configured implements Tool {
  
	private static CacheConfig cacheConf = null;
	protected FileSystem fs = null;
  @Override
  public int run(String[] args) throws Exception {

    Configuration conf = getConf();
    if (fs == null) {
        this.fs = FileSystem.get(conf);
    	}
    if (cacheConf == null) cacheConf = new CacheConfig(conf);
    Path f = new Path("/tmp", "hfile");
    HFileContext context = new HFileContextBuilder().withIncludesTags(false).build();
    Writer w =
        HFile.getWriterFactory(conf, cacheConf).withPath(fs, f).withFileContext(context).create();
    w.close();
    return 0;
  }
  
  public static void main(String[] args) throws Exception {
    int exitCode = ToolRunner.run(new EmptyHBaseFile(), args);
    System.exit(exitCode);
  }
}
