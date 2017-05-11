package com.sa.npopa.samples.ccs.v01;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallQueue
{
	private static final Logger LOG = LoggerFactory.getLogger(CallQueue.class);
    private static CallQueue instance;

    private LinkedBlockingQueue<Call> queue;

	private static final int QUEUE_MAX_TIME=60*1000; //10s - after this the call will DROP
	private static final int QUEUE_AVG_TIME=5*1000; //5s
	private static final int QUEUE_JITTER=3*1000; //3s - calls stay in the queue 2-8s.
	
	static Random random = new Random();
    
	
    public static synchronized void queueCall( Call call )
    {
        try
        {
            LOG.debug( "Queueing call " + call.getUuid());
            getInstance().queue.put(call);
        }
        catch ( InterruptedException e )
        {
            LOG.debug( "There was an error queueing the call" );
        }
    }

    public static synchronized Call retrieveCall()
    {
    	long now=System.currentTimeMillis();
    	Call call = getInstance().queue.peek();
    	if(call!=null){
    		if(call.getRingTime() > (QUEUE_AVG_TIME - QUEUE_JITTER + random.nextInt(2*QUEUE_JITTER))){
    			Call returncall = getInstance().queue.poll();
    			return returncall;
    		} 
    	}
    	return null;
    }
    
    public static synchronized void checkAndDropExpiredCalls(){
    	Call call = getInstance().queue.peek();
    	if(call!=null){
	    	if (call.getRingTime() > QUEUE_MAX_TIME){
	    		Call dropcall = getInstance().queue.poll();
	    		dropcall.setStatus(CallStatus.DROPPED);
	    	}
    	}
    }
    
    private static synchronized CallQueue getInstance()
    {
        if ( instance == null )
        {
            instance = new CallQueue();
        }
        return instance;
    }

    private CallQueue()
    {
        this.queue = new LinkedBlockingQueue<Call>();
    }
    
    public static int getSize()
    {
        return((getInstance().queue).size());
    }
    
    public static String dump(){
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getInstance().queue.toString());
    	
    	return sb.toString();
    }
    
    public static String stats(){
    	StringBuilder sb = new StringBuilder();
    	HashMap<CallStatus, Integer> stats = new HashMap<CallStatus, Integer>();
    	
    	Iterator<Call> i= getInstance().queue.iterator();
    	while(i.hasNext()){
    		CallStatus s=i.next().getStatus();
    		if (stats.containsKey(s)){
    			stats.put(s,(stats.get(s))+1);
    		} else {
    			stats.put(s,1);
    		}
    	}
    	sb.append(stats.toString());
    	return sb.toString();
    }
    
    public static String dump(int n){
    	StringBuilder sb = new StringBuilder();
    	Iterator<Call> i= getInstance().queue.iterator();
    	int counter=0;
    	while(i.hasNext() && counter<n){
    		sb.append(i.next().toString()+" ");
    		counter++;
    	}
	
    	return sb.toString();
    }
    
}