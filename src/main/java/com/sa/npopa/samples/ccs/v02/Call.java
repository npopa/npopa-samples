package com.sa.npopa.samples.ccs.v02;


import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Call
    implements Serializable
{
	private static final Logger LOG = LoggerFactory.getLogger(Call.class);
	private static final long serialVersionUID = 1L;

	private int duration;
    private int number;
    private int hold=0;
    private UUID uuid;
    private long startTime;
    private long endTime; 


    public Call( int number, int duration )
    {   
    	Random random = new Random();
        this.number = number;
        this.duration = duration;
        this.uuid = UUID.randomUUID();
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + duration*1000;
        if(1+random.nextInt(99)<20) 
        	hold=1;
        LOG.debug("Creating a call:"+ this.toString());
        LogQueue.queueLog(this.toJson());
    }

	public int getDuration() {
		return duration;
	}
	public int isHoldable() {
		return hold;
	}
	public void setHold(int hold) {
		this.hold = hold;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "Call [number=" + number + ", uuid=" + uuid + ", duration=" + duration + ", startTime=" + startTime
				+ ", endTime=" + endTime + "]";
	}

	public String toJson() {
		
		return "{"
	            + "\"type\"=\"" + "NEWCALL" + "\", "
				+ "\"id\"=\"" + uuid + "\", "
				+ "\"timestamp\"=\"" + startTime + "\""
			    + "}"; 
	}

}
