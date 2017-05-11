package com.sa.npopa.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileContextBuilder;
import org.apache.hadoop.hbase.io.hfile.HFile.Writer;
import org.apache.hadoop.hbase.mob.MobUtils;
import org.apache.hadoop.hbase.mob.filecompactions.PartitionedMobFileCompactor;
import org.apache.hadoop.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.hbase.util.Threads;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * kinit -kt /var/run/cloudera-scm-agent/process/1967-hbase-HBASETHRIFTSERVER/hbase.keytab hbase/npsec-gw.vpc.cloudera.com@AD.SEC.CLOUDERA.COM
 * export HBASE_CLASSPATH="npopa-samples-0.0.1-SNAPSHOT.jar:log4j.properties"
 * hbase com.sa.npopa.samples.MobCompactionByPartition --tableName table1 --columnFamily J --partition 159426f4caddb238eadb18d2c15eb10520160801
 * 
 */

public class MobCompactionByPartition extends Configured implements Tool {

	private static final Log LOG = LogFactory.getLog(MobCompactionByPartition.class);
	private Options options = new Options();
	private boolean verbose;

	private static String tableName;// = "table1";
	private static String family;// = "J";
	private static String partition;// ="feae3f501e5e3a312ff2999f9789915c20160801";
	private static String dummyUUID = "00000000000000000000000000000000"; // TODO or maybe I should generate a random UUID
	private HColumnDescriptor hcd;
	private Configuration conf = HBaseConfiguration.create();
	private CacheConfig cacheConf = new CacheConfig(conf);
	private FileSystem fs;
	private static ExecutorService pool;
	private List<FileStatus> allFiles = new ArrayList<>();

	public MobCompactionByPartition() {
		super();
		init();
	}

	public MobCompactionByPartition(Configuration conf) {
		super(conf);
		init();
	}

	private void init() {
		options.addOption("v", "verbose", false, "Verbose output; Not implemented");
		options.addOption("t", "tableName", true, "table name ie. table1");
		options.addOption("f", "columnFamily", true, "column family ie. J");
		options.addOption("p", "partition", true,
				"mob partition including the day ie. feae3f501e5e3a312ff2999f9789915c20160801");
	}

	public boolean parseOptions(String args[]) throws ParseException, IOException {
		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MobCompactionByPartition", options, true);
			return false;
		}
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		verbose = cmd.hasOption("v");
		if (cmd.hasOption("t")) {
			tableName = cmd.getOptionValue("t");
		}
		if (cmd.hasOption("f")) {
			family = cmd.getOptionValue("f");
		}
		if (cmd.hasOption("p")) {
			partition = cmd.getOptionValue("p");
		}

		return true;
	}

	@Override
	public int run(String[] args) throws Exception {

		if (getConf() == null) {
			throw new RuntimeException("A Configuration instance must be provided.");
		}

		try {
			if (!parseOptions(args))
				return 1;
		} catch (IOException ex) {
			LOG.error("Error parsing command-line options", ex);
			return 1;
		} catch (ParseException ex) {
			LOG.error("Error parsing command-line options", ex);
			return 1;
		}

		hcd = new HColumnDescriptor(family);

		/**
		 * Ideally this should be set in CM HBase Client Advanced Configuration
		 * Snippet (Safety Valve) for hbase-site.xml
		 * <property><name>hfile.format.version</name>
		 * <value>3</value></property> but just in case is not set:
		 */
		conf.set("hfile.format.version", "3");

		if (fs == null) {
			this.fs = FileSystem.get(conf);
		}
		if (cacheConf == null)
			cacheConf = new CacheConfig(conf);
		
		pool = createThreadPool(conf);

		Path mobTableCFPath = MobUtils.getMobFamilyPath(conf, TableName.valueOf(tableName), family);

		// create a dummy file to force compaction for partition
		Path dummy = Path.mergePaths(mobTableCFPath, new Path("/" + partition + dummyUUID));
		HFileContext context = new HFileContextBuilder().withIncludesTags(false).build();

		Writer w = HFile.getWriterFactory(conf, cacheConf).withPath(fs, dummy).withFileContext(context).create();
		w.close();

		Path path = Path.mergePaths(mobTableCFPath, new Path("/" + partition + "*"));
		PartitionedMobFileCompactor compactor = new PartitionedMobFileCompactor(conf, fs, TableName.valueOf(tableName),
				hcd, pool);

		// TODO not sure how this will perform in a 400k+ folder ?
		FileStatus[] mobfilesfiles = fs.globStatus(path);

		for (FileStatus f : mobfilesfiles) {
			allFiles.add(f);
		}

		compactor.compact(allFiles, true);

		DumpConfig();

		return 0;
	}

	private static ExecutorService createThreadPool(Configuration conf) {
		int maxThreads = 10;
		long keepAliveTime = 60;
		final SynchronousQueue<Runnable> queue = new SynchronousQueue<Runnable>();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(1, maxThreads, keepAliveTime, TimeUnit.SECONDS, queue,
				Threads.newDaemonThreadFactory("MobFileCompactionChore"), new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						try {
							// waiting for a thread to pick up instead of
							// throwing exceptions.
							queue.put(r);
						} catch (InterruptedException e) {
							throw new RejectedExecutionException(e);
						}
					}
				});
		((ThreadPoolExecutor) pool).allowCoreThreadTimeOut(true);
		return pool;
	}

	private void DumpConfig() {
		for (Entry<String, String> entry : conf) {
			System.out.printf("%s=%s\n", entry.getKey(), entry.getValue());
		}
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new MobCompactionByPartition(), args);
		System.exit(exitCode);
	}
}
