package com.sa.npopa.samples.ccs.v02;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCAgent
    implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(CCAgent.class);
	
    private Random random;

    private boolean running;
    private long researchTimeEnd=0;

    private String name;
    private CCAgentStatus status;

    public CCAgent( int id )
    {

        this.name="agent"+String.format("%04d",id);
        setStatus(CCAgentStatus.AVAILABLE);
        random = new Random();
    }

    
    @Override
    public void run()
    {
        Call call=null;
        while ( running )
        {
            if ( status == CCAgentStatus.AVAILABLE ) //can take a call or go on a break (not yet)
            {
            	
                call = CallQueue.retrieveCall();
                if ( call != null )
                {
                	LOG.debug( "[" + name + "] is answering call" + " ["+call.getNumber()+"]" + " ["+call.getUuid()+"]" );
                	LogQueue.queueLog("{"
	    	            + "\"type\"=\"" + "ANSWER" + "\", "
	    				+ "\"id\"=\"" + call.getUuid() + "\", "
	    			    + "\"agent\"=\"" + name + "\", "
	    				+ "\"timestamp\"=\"" + System.currentTimeMillis() + "\""
	    			    + "}");
                    setStatus(CCAgentStatus.BUSY);

                }
            } else if (status == CCAgentStatus.BUSY) //can hang-up, place it on hold or transfer the call (not yet)
            {
                if ( System.currentTimeMillis() >= call.getEndTime() ) //hang-up
                {
                	LOG.debug( "Call ["+call.getNumber()+"]" + " ["+call.getUuid()+"] finished."+" [" + name + "] is hanging up" );
                	LogQueue.queueLog("{"
	    	            + "\"type\"=\"" + "HANGUP" + "\", "
	    				+ "\"id\"=\"" + call.getUuid() + "\", "
	    			    + "\"agent\"=\"" + name + "\", "
	    				+ "\"timestamp\"=\"" + System.currentTimeMillis() + "\", "
	    	    		+ "\"duration\"=\"" + call.getDuration() + "\""    				
	    			    + "}");
                	setStatus(CCAgentStatus.WRAP_UP);
                } else if ( (call.isHoldable()==1) && (random.nextInt(100) < 10)) 
                {
                	
                	LOG.debug( "Call ["+call.getNumber()+"]" + " ["+call.getUuid()+"] is being researched by "+" [" + name + "]" );

                	LogQueue.queueLog("{"
    	    	            + "\"type\"=\"" + "ON_HOLD" + "\", "
    	    				+ "\"id\"=\"" + call.getUuid() + "\", "
    	    			    + "\"agent\"=\"" + name + "\", "
    	    				+ "\"timestamp\"=\"" + System.currentTimeMillis() +"\""    				
    	    			    + "}");
                    	setStatus(CCAgentStatus.RESEARCHING);
                    	researchTimeEnd=(long) (System.currentTimeMillis() + (call.getEndTime() - System.currentTimeMillis())*(25+random.nextInt(15))/100);
                    	call.setHold(0); //make sure it will not get on hold again
                }
            } else if (status == CCAgentStatus.WRAP_UP) //document the call for 5-10% of the original call duration
            {
                if ( System.currentTimeMillis() >= call.getEndTime() + (call.getDuration()*(random.nextInt(6)+5)/100)*1000)
                {
                	LOG.debug( "Call ["+call.getNumber()+"]" + " ["+call.getUuid()+"] finished."+" [" + name + "] is wrapping up" );
                	LogQueue.queueLog("{"
	    	            + "\"type\"=\"" + "WRAP_UP" + "\", "
	    				+ "\"id\"=\"" + call.getUuid() + "\", "
	    			    + "\"agent\"=\"" + name + "\", "
	    				+ "\"timestamp\"=\"" + System.currentTimeMillis() +"\""    				
	    			    + "}");
                	setStatus(CCAgentStatus.AVAILABLE);
              
                }
            } else if (status == CCAgentStatus.RESEARCHING) //get back to call if the hold is over
            {
                if ( System.currentTimeMillis() >= researchTimeEnd)
                {
                	LOG.debug( "Call ["+call.getNumber()+"]" + " ["+call.getUuid()+"] research finished by"+" [" + name + "]" );
                	LogQueue.queueLog("{"
	    	            + "\"type\"=\"" + "OFF_HOLD" + "\", "
	    				+ "\"id\"=\"" + call.getUuid() + "\", "
	    			    + "\"agent\"=\"" + name + "\", "
	    				+ "\"timestamp\"=\"" + System.currentTimeMillis() +"\""    				
	    			    + "}");
                	setStatus(CCAgentStatus.BUSY);
              
                }
            }
            sleep(); //one second and come back
        }
    }

    public void start()
    {
        running = true;
        new Thread(this).start();
    }

    public void stop()
    {
        running = false;
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(1000);
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }


	public CCAgentStatus getStatus() {
		return status;
	}


	public void setStatus(CCAgentStatus status) {
		this.status = status;
    	LogQueue.queueLog("{"
	            + "\"type\"=\"" + "AGENT_STATUS" + "\", "
			    + "\"agent\"=\"" + name + "\", "
				+ "\"timestamp\"=\"" + System.currentTimeMillis() + "\", "
	    		+ "\"status\"=\"" + status + "\""    				
			    + "}");		
	}
    
    
    
}