package com.sa.npopa.samples.ccs.v01;


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

	String[] CallTopics = new String[]{"Flume","HBase","HDFS","HIVE","HUE","Impala","Kafka","Oozie","Sentry","Solr","Spark","YARN","Zookeeper"};
	String[] CallSubTopics = new String[]{"architecture","coding","design","documentation","samples"};
//TODO Maybe insert some fault?
//Implement some call workflows. I think I need to implement some "experience" in agent and drive the duration based on it. But then topic and subtopic 


    private UUID uuid;

    private long ringTime=0;
    private long talkTime=0; 
    private long holdTime=0;
    private long docTime=0;

/**      
    private long RINGING_TS=0;  
    private long DROPPED_TS=0;
    private long ANSWERED_TS=0;
    private long ONHOLD_TS=0;
    private long HUNGUP_TS=0;
    private long COMPLETED_TS=0;
*/    
    private long lastTransition_TS=0; //this gets updated as the call progresses
    private CallStatus lastState = CallStatus.NONE;
    
    private String topic;
    private String subTopic;
    private CallStatus status = CallStatus.NONE;
    
    private Agent agent;


    public Call()
    {   
    	Random random = new Random();
        this.uuid = UUID.randomUUID();
        
        this.topic=CallTopics[random.nextInt(CallTopics.length)];
        this.subTopic=CallSubTopics[random.nextInt(CallSubTopics.length)];
        //once the call has been made make it ringing and place it to the queue
        this.setStatus(CallStatus.RINGING);

    }

	public long getRingTime() {
		long now=System.currentTimeMillis();
		if(status==CallStatus.RINGING){
			return ringTime+now-lastTransition_TS;
		}
		return ringTime;
	}

	public void setRingTime(long ringTime) {
		this.ringTime = ringTime;
	}

	public long getTalkTime() {
		long now=System.currentTimeMillis();
		if(status==CallStatus.INPROGRESS){
			return talkTime+now-lastTransition_TS;
		}
		return talkTime;
	}

	public void setTalkTime(long talkTime) {
		this.talkTime = talkTime;
	}

	public long getHoldTime() {
		long now=System.currentTimeMillis();
		if(status==CallStatus.ONHOLD){
			return holdTime+now-lastTransition_TS;
		}
		return holdTime;
	}

	public void setHoldTime(long holdTime) {
		this.holdTime = holdTime;
	}

	public long getDocTime() {
		long now=System.currentTimeMillis();
		if(status==CallStatus.FINISHED){
			return docTime+now-lastTransition_TS;
		}
		return docTime;
	}

	public void setDocTime(long docTime) {
		this.docTime = docTime;
	}
/*
	public long getElapsedTime() {	
		return (System.currentTimeMillis()-RINGING_TS);
	}
	
	public long getElapsedTimeSinceLastTransition() {	
		return (System.currentTimeMillis()-lastTransition_TS);
	}
*/	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
/*
	public long getStartTime() {
		return RINGING_TS;
	}

	public void setStartTime(long startTime) {
		this.RINGING_TS = startTime;
	}

	public long getEndTime() {
		return HUNGUP_TS;
	}
	

	public void setEndTime(long endTime) {
		this.HUNGUP_TS = endTime;
	}
*/
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getSubTopic() {
		return subTopic;
	}

	public void setSubTopic(String subTopic) {
		this.subTopic = subTopic;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public CallStatus getStatus() {
		return status;
	}

	

	@Override
	public String toString() {
		return "Call [uuid=" + uuid + ", ringTime=" + getRingTime() + ", lastTransition_TS=" + lastTransition_TS
				+ ", status=" + status + "]";
	}

	public synchronized void setStatus(CallStatus status) {
		lastState=this.status;
		this.status = status;
		String MSG_TYPE = "call";
		long now=System.currentTimeMillis();

		if (status==CallStatus.RINGING){
			//this.RINGING_TS = now;
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
							+ "\"type\":\"" + status + "\", "
							+ "\"id\":\"" + uuid + "\", "
							+ "\"timestamp\":\"" + now + "\""
							+ "}"
					));

		} else if (status==CallStatus.INPROGRESS){
			if(lastState==CallStatus.RINGING) {
				//ANSWERED_TS=now;
				ringTime=now-lastTransition_TS;
			} else if (lastState==CallStatus.ONHOLD){
				holdTime=holdTime + now -lastTransition_TS;
			}
			
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
							+ "\"type\":\"" + status + "\", "
							+ "\"id\":\"" + uuid + "\", "
							+ "\"agent\":\"" + agent.getName() + "\", "
							+ "\"callCenter\":\"" + agent.getCallCenter() + "\", "
							+ "\"timestamp\":\"" + now + "\""
							+ "}"
					));
		} else if (status==CallStatus.FINISHED){
			//HUNGUP_TS=now;
			talkTime=talkTime+now-lastTransition_TS;
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
							+ "\"type\":\"" + status + "\", "
							+ "\"id\":\"" + uuid + "\", "
							+ "\"agent\":\"" +  agent.getName() + "\", "
							+ "\"timestamp\":\"" + now + "\""			
							+ "}"

					));
		} else if (status==CallStatus.ONHOLD){
			//ONHOLD_TS=now;
			talkTime=talkTime+now-lastTransition_TS;
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
							+ "\"type\":\"" + status + "\", "
							+ "\"id\":\"" + uuid + "\", "
							+ "\"agent\":\"" +  agent.getName() + "\", "
							+ "\"timestamp\":\"" + now + "\""			
							+ "}"

					));
		} else if (status==CallStatus.COMPLETED){
			//COMPLETED_TS=now;
			docTime=docTime+now-lastTransition_TS;
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
		    	            + "\"type\":\"" + status + "\", "
		    				+ "\"id\":\"" + uuid + "\", "
		    			    + "\"agent\":\"" + agent.getName() + "\", "
				    		+ "\"topic\":\"" + topic + "\", "		
						    + "\"subtopic\":\"" + subTopic + "\", "	
		    				+ "\"timestamp\":\"" + now +"\""    				
		    			    + "}"

					));
		}else if (status==CallStatus.DROPPED){
			//DROPPED_TS=now;
			ringTime=now-lastTransition_TS;
			LogQueue.queueLog(new LogMessage(MSG_TYPE,uuid.toString(),
					"{"
		    	            + "\"type\":\"" + status + "\", "
		    				+ "\"id\":\"" + uuid + "\", "
		    				+ "\"timestamp\":\"" + now +"\""    				
		    			    + "}"

					));
		}

		lastTransition_TS=now;

	}

}
