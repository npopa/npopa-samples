package com.sa.npopa.samples.flume;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.time.DateUtils;


public class UDFs {
	
    private static MessageDigest messageDigest ;
	private static Random random = new Random();
	
	public static String timestampNormalizeToHour(String timestamp){
		
		Date truncatedDate = DateUtils.truncate(new Date(Long.parseLong(timestamp)), Calendar.HOUR);
		return  Long.toString(truncatedDate.getTime());

	}
	public static String timestampNormalizeToDate(String timestamp){
		
		Date truncatedDate = DateUtils.truncate(new Date(Long.parseLong(timestamp)), Calendar.DATE);
		return  Long.toString(truncatedDate.getTime());

	}
	public static String timestampDeltaFromHour(String timestamp){
		
		long delta = DateUtils.getFragmentInMilliseconds(new Date(Long.parseLong(timestamp)), Calendar.HOUR);
		return  Long.toString(delta);

	}
	public static String timestampDeltaFromDate(String timestamp){
		
		long delta = DateUtils.getFragmentInMilliseconds(new Date(Long.parseLong(timestamp)), Calendar.DATE);
		return  Long.toString(delta);

	}		
	
	public static String MD5(String string){
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageDigest.reset();
		byte[] resultByte = messageDigest.digest(string.getBytes());
		return Hex.encodeHexString(resultByte);
	}
	
	public static String MD5Prefix2(String string){
		return MD5(string).substring(0, 2);
	}

	public static String MD5Prefix4(String string){
		return MD5(string).substring(0, 4);
	}
	
	public static String Prefix2(String string){
		return string.substring(0, 2);
	}

	public static String Prefix4(String string){
		return string.substring(0, 4);
	}
	
	public static String RandomBytePrefix(String string){
		byte[] bytes = new byte[1];
		random.nextBytes(bytes);
		return (Hex.encodeHexString(bytes));
	}
	
	public static String TS2YYYYMMDDHH(String timestamp){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
		return dateFormat.format(new Date(Long.parseLong(timestamp)));
	}	
	
	public static String TS2YYYYMMDD(String timestamp){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return dateFormat.format(new Date(Long.parseLong(timestamp)));
	}

	public static String GetAgentIncrement(String type){

		long result;
		//TODO - consider encoding some states on less than 8bits if more states are needed
		if(type.equals("AVAILABLE")){
			result=0x0000000000000001L;
		}else if(type.equals("TALKING")) {
			result=0x0000000000000100L;
		}else if(type.equals("RESEARCHING")) {
			result=0x0000000000010000L;
		}else if(type.equals("DOCUMENTING")) {
			result=0x0000000001000000L;
		}else if(type.equals("UNUSED1")) {
			result=0x0000000100000000L;
		}else if(type.equals("UNUSED2")) {
			result=0x0000010000000000L;
		}else if(type.equals("ON_BREAK")) {
			result=0x0001000000000000L;
		}else if(type.equals("OFFLINE")) {
			result=0x0100000000000000L;
		}else 
			result=0x1000000000000000L; //this is an error

		return Long.toString(result);
	}

	public static String GetCallIncrement(String type){
		
		long result;
		//TODO - consider encoding some states on less than 8bits if more states are needed
		if(type.equals("RINGING")){
			result=0x0000000000000001L;
		}else if(type.equals("INPROGRESS")) {
			result=0x0000000000000100L;
		}else if(type.equals("ONHOLD")) {
			result=0x0000000000010000L;
		}else if(type.equals("FINISHED")) {
			result=0x0000000001000000L;
		}else if(type.equals("COMPLETED")) {
			result=0x0000000100000000L;
		}else if(type.equals("DROPPED")) {
			result=0x0000010000000000L;
		}else if(type.equals("UNUSED")) {
			result=0x0001000000000000L;
		}else if(type.equals("METRICS")) {
			result=0x0100000000000000L;
		}else 
			result=0x1000000000000000L; //ERROR

		return Long.toString(result);
	}	
	
}
