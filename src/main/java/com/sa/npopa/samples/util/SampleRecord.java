package com.sa.npopa.samples.util;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

public class SampleRecord {
    
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
