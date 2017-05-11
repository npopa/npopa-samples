package com.sa.npopa.samples.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;



public class GenerateSampleFiles extends Configured implements Tool {
	
	private Options options = new Options();
	private long numRecords=0;
	private String outputPath;
	

	
	public static class SampleRecord {
	    
		private  String key;
		private long ts = System.currentTimeMillis();
		Random rand = new Random();
		private int maxAttributes;

		
		public SampleRecord(int maxAttributes){
			
	        this.maxAttributes=maxAttributes;
			key= UUID.randomUUID().toString();
		}

		public String getMessage(){	
			
			long numAttributes = 1+rand.nextInt(maxAttributes-1);
			
			StringBuilder result = new StringBuilder();

			result.append("{");
			result.append("\"id\":\""+key+"\",");	
			result.append("\"timestamp\":\""+ts+"\",");	
	        for(int i=0;i<numAttributes;i++)
	        {
	        	byte[] bytes= new byte[1+rand.nextInt(25)];
	        	rand.nextBytes(bytes);
	        	result.append("\"name"+String.format("%02d", i+1)+"\":\""+Hex.encodeHexString(bytes)+"\",");
	        	
	        }
			result.append("\"atrributes\":\""+numAttributes+"\"");		
			result.append("}");
		    
		    return result.toString();
		}

		
		public String getKey(){
		    return key;
		}		
			
	}

	
	@Override
	public int run(String[] args) throws Exception {
		
		
		init() ;
		
		try {
		if (!parseOptions(args))
			return 1;
	} catch (IOException ex) {

		return 1;
	}
		
		Random rand = new Random();

	    long startTime = System.currentTimeMillis();
		File file1 = new File(outputPath+"/col1");
		// if file doesnt exists, then create it
		if (!file1.exists()) {
			file1.createNewFile();
		}
		FileWriter fw1 = new FileWriter(file1.getAbsoluteFile());
		BufferedWriter bw1 = new BufferedWriter(fw1);

		File file2 = new File(outputPath+"/col2");
		// if file doesnt exists, then create it
		if (!file2.exists()) {
			file2.createNewFile();
		}
		FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
		BufferedWriter bw2 = new BufferedWriter(fw2);		
		
		for (int i = 1; i <= numRecords; i++) { //generate 10000 records into the main file
			SampleRecord m = new SampleRecord(10);
			bw1.write(m.getMessage() + "\n");
			if (rand.nextInt(100)<75) //only 75% go into the second file
			{
				bw2.write(m.getMessage() + "\n");
			}
		}

		bw1.close();
		bw2.close();
	    System.out.println("Elapsed "+ (System.currentTimeMillis()-startTime)+" ms.");
		return 0;
	}

	private void init() {

		options.addOption("o", "outputPath", true, "outputPath");
		options.addOption("n", "numRecords", true, "numRecords");

		}

	public boolean parseOptions(String args[]) throws ParseException, IOException {
		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("GenerateSampleFiles", options, true);
			return false;
		}
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);	

		if (cmd.hasOption("o")) {
			outputPath = cmd.getOptionValue("o");
		} 
		
		if (cmd.hasOption("n")) {
			String records = cmd.getOptionValue("n");
			numRecords = Long.valueOf(records);
		}
		



		return true;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new GenerateSampleFiles(), args);
		System.exit(exitCode);
	}
}
