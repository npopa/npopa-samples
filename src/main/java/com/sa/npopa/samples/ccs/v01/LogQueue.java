package com.sa.npopa.samples.ccs.v01;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogQueue
{
	private static final Logger LOG = LoggerFactory.getLogger(LogQueue.class);
    private static LogQueue instance;

    private LinkedBlockingQueue<LogMessage> queue;

    public static void queueLog( LogMessage logMessage )
    {
        try
        {
            LOG.debug( "Queueing log message: " + logMessage );
            getInstance().queue.put(logMessage);
        }
        catch ( InterruptedException e )
        {
            LOG.error( "There was an error queueing the call" );
        }
    }

    public static LogMessage retrieveLog()
    {
    	LogMessage logMessage = getInstance().queue.poll();
        if ( logMessage != null )
        {
            LOG.trace( "Retrieving log " + logMessage );
        }
        return logMessage;
    }

    
    private static LogQueue getInstance()
    {
        if ( instance == null )
        {
            instance = new LogQueue();
        }
        return instance;
    }

    private LogQueue()
    {
        this.queue = new LinkedBlockingQueue<LogMessage>();

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
}