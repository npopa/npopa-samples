package com.sa.npopa.samples.util;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

public class SampleMessage {
    
	private  String message;
	private  String key;
	private long ts = System.currentTimeMillis();

	
	public SampleMessage(int payloadSize){
		message=Hex.encodeHexString(new RandomByteArray().getRandomByteArray(payloadSize/2));
		key= UUID.randomUUID().toString();
	}

	public String getMessage(){
		StringBuilder result = new StringBuilder();

		result.append("{");
		result.append("\"id\": \""+key+"\",");	
		result.append("\"timestamp\": \""+ts+"\",");	
		result.append("\"payload_size\": \""+message.length()+"\",");
		result.append("\"payload\": \""+message+"\",");
		result.append("\"md5\": \""+MD5(message)+"\"");		
		result.append("}");
	    
	    return result.toString();
	}	
	public String getKey(){
	    return key;
	}		
	
	
	private String MD5(String md5) {
		   try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
		        byte[] array = md.digest(md5.getBytes());
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
		}
	
}
