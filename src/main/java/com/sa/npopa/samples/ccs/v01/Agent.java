package com.sa.npopa.samples.ccs.v01;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//TODO implement some kind of experience tracker in the agents
//agent shifts count should be a weighted distribution (not linear) 
public class Agent
{
	private static final Logger LOG = LoggerFactory.getLogger(Agent.class);
	
	int COFFEE_BREAK=5*60*1000; //5 min
	
	int AVG_BREAK_INTERVAL=120*60*1000; //every two hours
    int BREAK_INTERVAL_JITTER=30*60*1000; //4 min
    
    private int HOLD_AVG_TIME=1*60*1000; //1 min
    private int HOLD_JITTER=30*1000; //30 sec
    private int HOLD_PROBABILITY=20; //this agent is 20% likely to put the call on hold
	
    private int TALK_AVG_TIME=4*60*1000; //4 min
    private int TALK_JITTER=2*60*1000; //2 min
    
    private int DOC_AVG_TIME=1*60*1000; //1 min
    private int DOC_JITTER=30*1000; //30 sec
    
    private int[] agentShiftDistribution = {25,25,50};

    private String name;
    private AgentStatus status;
    private Call call;
    private boolean locked=false;
    private long agentShift;
    
    private long lastBreak;
    private String callCenter;
    
	Random random = new Random();
	
    private long projectedTalkTime=0; 
    private long projectedHoldTime=0;
    private long projectedDocTime=0;
    private long projectedHoldPoint=0;

    
    
	String[] CallCenters = new String[]{"Austin","Palo Alto","New York","Chicago"};

    public Agent(int id)
    {
        this.name="agent"+String.format("%05d",id);
    	Random random = new Random();
    	int randomShift=random.nextInt(100);
    	if(randomShift<agentShiftDistribution[0]) {
    		this.agentShift=0;
    	}else if(randomShift<agentShiftDistribution[0]+agentShiftDistribution[1]) {
    		this.agentShift=1;
    	} else {
    		this.agentShift=2;
    	}
    	
        

        status = AgentStatus.OFFLINE; //all agents will be offline by default. Not calling setStatus() here intentionally.
      
        callCenter=CallCenters[random.nextInt(CallCenters.length)];
        
        
    }
/**
    public void handleBreaks(){
    	
    	long now=System.currentTimeMillis();

    	if ((status==AgentStatus.AVAILABLE) && (now >= lastBreak+BREAK_INTERVAL+random.nextInt((int) (BREAK_INTERVAL*.1)))){
    		lastBreak=now;
    		this.setStatus(AgentStatus.ON_BREAK);
    	}

    	if ((status==AgentStatus.ON_BREAK) && (now >= lastBreak+COFFEE_BREAK+random.nextInt((int) (COFFEE_BREAK*.1)))){
    		lastBreak=now;
    		this.setStatus(status=AgentStatus.AVAILABLE);
    	}
    	
    }
    
    public void handleShifts(){

    	
        if ((hour>=agentShift*8 && hour<(agentShift+1)*8) && status==AgentStatus.OFFLINE) {   //only the agents working this shift would be available  
            setStatus(AgentStatus.AVAILABLE);
            lastBreak=System.currentTimeMillis(); //reset the last break when the shift starts
        }  
        
        if (!(hour>=agentShift*8 && hour<(agentShift+1)*8) && status==AgentStatus.AVAILABLE) {    
            setStatus(AgentStatus.OFFLINE);
        }   
    }
*/  
    public boolean isShiftStarted(){
    	Calendar calendar_now = Calendar.getInstance();
    	int hour=calendar_now.get(Calendar.HOUR_OF_DAY);
    	return (hour>=agentShift*8 && hour<(agentShift+1)*8);
    }
    
    public boolean isShiftOver(){
    	return !isShiftStarted();
    }   

    public boolean isOffline(){
    	return (status == AgentStatus.OFFLINE);
    } 

    public boolean isOnline(){
    	return !(status == AgentStatus.OFFLINE);
    } 
    
    public  void work(){

    	LOG.debug("Agent" + name + " Status is "+this.status);
    	long now=System.currentTimeMillis();
    	Calendar calendar_now = Calendar.getInstance();
    	int hour=calendar_now.get(Calendar.HOUR_OF_DAY);

    	if (status==AgentStatus.AVAILABLE){
    		
    		if(!(hour>=agentShift*8 && hour<(agentShift+1)*8)){ //shift is over
    			setStatus(AgentStatus.OFFLINE);

    		} else if (now >= lastBreak+(AVG_BREAK_INTERVAL - BREAK_INTERVAL_JITTER + random.nextInt(2*BREAK_INTERVAL_JITTER))) {
    			lastBreak=now;
    			this.setStatus(AgentStatus.ON_BREAK); //need to revisit this and see if I can spread the breaks better
    		} else {
    			//pick up a new call and return
    			Call newcall = CallQueue.retrieveCall();
    			if (newcall!=null){ //new call?

    				call=newcall;
    				call.setAgent(this);
    				this.setCall(call);
    				LOG.debug( "Call ["+call.getUuid()+"] is answered."+" [" + this.getName() + "] is talking" );
    				call.setStatus(CallStatus.INPROGRESS);
    				this.setStatus(AgentStatus.TALKING); 

    				//set the projected parameters for this call.
    				projectedTalkTime=(TALK_AVG_TIME - TALK_JITTER + random.nextInt(2*TALK_JITTER)); 
    				projectedDocTime=(DOC_AVG_TIME - DOC_JITTER + random.nextInt(2*DOC_JITTER));
    				if (random.nextInt(100)<=HOLD_PROBABILITY){
    					projectedHoldTime=(HOLD_AVG_TIME - HOLD_JITTER + random.nextInt(2*HOLD_JITTER));
    					projectedHoldPoint=projectedTalkTime*(25+random.nextInt(50))/100; //hold at some point between 25% and 75% of the call.
    				}


    			}
    		}
    	} else if (status==AgentStatus.ON_BREAK){
    		//check if any transition required
    		if ( (now >= lastBreak+COFFEE_BREAK+random.nextInt((int) (COFFEE_BREAK*.1))))
    		{
    			this.setStatus(AgentStatus.AVAILABLE);	              
    		}
    	}else if (status==AgentStatus.TALKING){
    		//check if any transition required
    		if ( call.getTalkTime() > projectedTalkTime) // talk time is up
    		{
    			LOG.debug( "Call ["+call.getUuid()+"] finished."+" [" + this.getName() + "] is hanging up" );
    			call.setStatus(CallStatus.FINISHED);
    			this.setStatus(AgentStatus.DOCUMENTING);
    		} else if ((projectedHoldTime>0) && (call.getTalkTime() > projectedHoldPoint)){
    			LOG.debug( "Call ["+call.getUuid()+"] is on hold."+" [" + this.getName() + "] is researching" );
    			call.setStatus(CallStatus.ONHOLD);
    			this.setStatus(AgentStatus.RESEARCHING);
    		}
    	} else if (status==AgentStatus.RESEARCHING){
    		//check if any transition required
    		if ( call.getHoldTime() > projectedHoldTime) // hold is over?
    		{
    			LOG.debug( "Call ["+call.getUuid()+"] is off hold."+" [" + this.getName() + "] is talking" );
    			call.setStatus(CallStatus.INPROGRESS);
    			this.setStatus(AgentStatus.TALKING);
    			projectedHoldTime=0;//no more holds
    		} 
    	} else if (status==AgentStatus.DOCUMENTING){
    		//check if any transition required
    		if ( call.getDocTime() > projectedDocTime)
    		{
    			LOG.debug( "Call ["+call.getUuid()+"] finished."+" [" + this.getName() + "] is wrapping up" );
    			call.setStatus(CallStatus.COMPLETED);
    			this.setStatus(AgentStatus.AVAILABLE);	              
    		}
    	}

    }
    
	public AgentStatus getStatus() {
		return status;
	}
	public String getName() {
		return name;
	}
	
	public void setCall(Call call)
	{
		this.call=call;
	}
	public Call getCall()
	{
		return(this.call);
	}
	public synchronized boolean isLocked()
	{
		return(this.locked);
	}
	public synchronized void setLock(boolean locked)
	{
		LOG.trace( "Agent "+name+" lock is " + locked );
	    this.locked=locked;
	}
	public String getCallCenter() {
		return callCenter;
	}

	public void setCallCenter(String callCenter) {
		this.callCenter = callCenter;
	}
	
	public long getLastBreak() {
		return lastBreak;
	}
	public void setLastBreak(long lastBreak) {
		this.lastBreak = lastBreak;
	}
	@Override
	public String toString() {
		return "Agent [name=" + name + ", status=" + status + ", agentShift=" + agentShift + ", callCenter="
				+ callCenter + "]";
	}
	public void  setStatus(AgentStatus status)  {
		this.status = status;
		
    	LogQueue.queueLog(new LogMessage
    			("agent",
    			name,
    			"{"
			    + "\"agent\":\"" + name + "\", "
				+ "\"timestamp\":\"" + System.currentTimeMillis() + "\", "
	    		+ "\"status\":\"" + status + "\""    				
			    + "}"));		
	}


    
}