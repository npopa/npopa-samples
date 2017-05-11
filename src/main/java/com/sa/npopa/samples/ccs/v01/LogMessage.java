package com.sa.npopa.samples.ccs.v01;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogMessage
{
	private static final Logger LOG = LoggerFactory.getLogger(LogMessage.class);
//TODO Maybe all log messages should have a token?	
	
    private String type;
    private String key;
    private String msg;
 
	public LogMessage(String type, String key, String msg) {

		this.type = type;
		this.key = key;
		this.msg = msg;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return "LogMessage [type=" + type + ", key=" + key + ", msg=" + msg + "]";
	}


}